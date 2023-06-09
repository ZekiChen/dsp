package com.tecdo.service.init;

import com.google.common.base.MoreObjects;
import com.tecdo.adm.api.doris.entity.BundleData;
import com.tecdo.adm.api.doris.mapper.ReportMapper;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.core.launch.thread.ThreadPool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleDataManager {

  private final SoftTimer softTimer;
  private final MessageQueue messageQueue;
  private final ThreadPool threadPool;

  private State currentState = State.INIT;
  private long timerId;

  private Set<String> impGtSizeSet;

  private Map<String, BundleData> bundleDataMap;

  private final ReportMapper reportMapper;

  @Value("${pac.timeout.load.db.default}")
  private long loadTimeout;
  @Value("${pac.interval.reload.bundle.data}")
  private long reloadInterval;
  @Value("${pac.bundle.test.cycle.day}")
  private int cycleTime;
  @Value("${pac.bundle.test.size}")
  private int size;

  public Set<String> getImpGtSizeSet() {
    return impGtSizeSet;
  }

  public Map<String, BundleData> getBundleDataMap() {
    return bundleDataMap;
  }


  @AllArgsConstructor
  private enum State {
    INIT(1, "init"),
    WAIT_INIT_RESPONSE(2, "waiting init response"),
    RUNNING(3, "init success, now is running"),
    UPDATING(4, "updating");

    private int code;
    private String desc;

    @Override
    public String toString() {
      return code + " - " + desc;
    }
  }

  public void init(Params params) {
    messageQueue.putMessage(EventType.BUNDLE_DATA_LOAD, params);
  }

  private void startReloadTimeoutTimer(Params params) {
    timerId = softTimer.startTimer(EventType.BUNDLE_DATA_LOAD_TIMEOUT, params, loadTimeout);
  }

  private void cancelReloadTimeoutTimer() {
    softTimer.cancel(timerId);
  }

  private void startNextReloadTimer(Params params) {
    softTimer.startTimer(EventType.BUNDLE_DATA_LOAD, params, reloadInterval);
  }

  public void switchState(State state) {
    this.currentState = state;
  }

  public void handleEvent(EventType eventType, Params params) {
    switch (eventType) {
      case BUNDLE_DATA_LOAD:
        handleReload(params);
        break;
      case BUNDLE_DATA_LOAD_RESPONSE:
        handleResponse(params);
        break;
      case BUNDLE_DATA_LOAD_ERROR:
        handleError(params);
        break;
      case BUNDLE_DATA_LOAD_TIMEOUT:
        handleTimeout(params);
        break;
      default:
        log.error("Can't handle event, type: {}", eventType);
    }
  }

  private void handleReload(Params params) {
    switch (currentState) {
      case INIT:
      case RUNNING:
        threadPool.execute(() -> {
          try {
            Calendar start = Calendar.getInstance();
            start.add(Calendar.DAY_OF_MONTH, -cycleTime);
            start.add(Calendar.DAY_OF_MONTH, -1);

            Calendar end = Calendar.getInstance();
            end.add(Calendar.DAY_OF_MONTH, -1);
            List<BundleData> bundleData =
              reportMapper.getBundleData(DateUtil.format(new Date(), "yyyy-MM-dd_HH"));
            List<BundleData> dataImpCountGtSize =
              reportMapper.getDataImpCountGtSize(DateUtil.format(start.getTime(), "yyyy-MM-dd"),
                                                 DateUtil.format(end.getTime(), "yyyy-MM-dd"),
                                                 size);
            params.put(ParamKey.BUNDLE_DATA_GT_SIZE_CACHE_KEY,
                       dataImpCountGtSize.stream().map(this::makeKey).collect(Collectors.toSet()));
            params.put(ParamKey.BUNDLE_DATA_CACHE_KEY,
                       bundleData.stream()
                                 .collect(Collectors.toMap(this::makeKey,
                                                           Function.identity(),
                                                           (o, n) -> n)));
            messageQueue.putMessage(EventType.BUNDLE_DATA_LOAD_RESPONSE, params);
          } catch (Exception e) {
            log.error("bundle data load failure from db", e);
            messageQueue.putMessage(EventType.BUNDLE_DATA_LOAD_ERROR, params);
          }
        });
        startReloadTimeoutTimer(params);
        switchState(currentState == State.INIT ? State.WAIT_INIT_RESPONSE : State.UPDATING);
        break;
      default:
        log.error("Can't handle event, state: {}", currentState);
    }
  }

  private void handleResponse(Params params) {
    switch (currentState) {
      case WAIT_INIT_RESPONSE:
        messageQueue.putMessage(EventType.ONE_DATA_READY);
        // set when init
        this.impGtSizeSet = params.get(ParamKey.BUNDLE_DATA_GT_SIZE_CACHE_KEY);
        this.bundleDataMap = params.get(ParamKey.BUNDLE_DATA_CACHE_KEY);
        // no break
      case UPDATING:
        cancelReloadTimeoutTimer();
        this.impGtSizeSet = params.get(ParamKey.BUNDLE_DATA_GT_SIZE_CACHE_KEY);
        Map<String, BundleData> bundleData = params.get(ParamKey.BUNDLE_DATA_CACHE_KEY);
        bundleData.forEach((key, v) -> {
          v.setOldK(this.bundleDataMap.getOrDefault(key, v).getK());
          // todo 这里需要有淘汰机制，不然不断往里面加数据，可能会导致内存溢出
          this.bundleDataMap.put(key, v);
        });
        log.info("bundle data load success, impGtSizeSet size: {},bundleDataMap size:{}",
                 impGtSizeSet.size(),
                 bundleDataMap.size());
        startNextReloadTimer(params);
        switchState(State.RUNNING);
        break;
      default:
        log.error("Can't handle event, state: {}", currentState);
    }
  }

  private void handleError(Params params) {
    switch (currentState) {
      case WAIT_INIT_RESPONSE:
      case UPDATING:
        cancelReloadTimeoutTimer();
        startNextReloadTimer(params);
        switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
        break;
      default:
        log.error("Can't handle event, state: {}", currentState);
    }
  }

  private void handleTimeout(Params params) {
    switch (currentState) {
      case WAIT_INIT_RESPONSE:
      case UPDATING:
        log.error("timeout load bundle data");
        startNextReloadTimer(params);
        switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
        break;
      default:
        log.error("Can't handle event, state: {}", currentState);
    }
  }

  private String makeKey(BundleData bundleData) {
    return bundleData.getCountry()
                     .concat("_")
                     .concat(bundleData.getBundle())
                     .concat("_")
                     .concat(bundleData.getAdFormat())
                     .concat("_")
                     .concat(MoreObjects.firstNonNull(bundleData.getAdWidth(), ""))
                     .concat("_")
                     .concat(MoreObjects.firstNonNull(bundleData.getAdHeight(), ""));
  }

}
