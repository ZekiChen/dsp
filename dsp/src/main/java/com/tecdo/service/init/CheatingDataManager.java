package com.tecdo.service.init;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.tecdo.adm.api.doris.entity.CheatingData;
import com.tecdo.adm.api.doris.mapper.CheatingDataMapper;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.core.launch.thread.ThreadPool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheatingDataManager {

  private final SoftTimer softTimer;
  private final MessageQueue messageQueue;
  private final ThreadPool threadPool;

  private State currentState = State.INIT;
  private long timerId;

  private final CheatingDataMapper mapper;

  private BloomFilter<CharSequence> ipFilter;
  private BloomFilter<CharSequence> deviceIdFilter;

  @Value("${pac.timeout.load.cheating.data}")
  private long loadTimeout;
  @Value("${pac.interval.reload.cheating.data}")
  private long reloadInterval;

  @Value("${pac.cheating.data.load.batch-size}")
  private int batchSize;

  @Value("${pac.cheating.data.fpp}")
  private double fpp;

  public boolean ipCheck(String ip) {
    return !ipFilter.mightContain(ip);
  }

  public boolean deviceIdCheck(String deviceId) {
    return !deviceIdFilter.mightContain(deviceId);
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
    messageQueue.putMessage(EventType.CHEATING_DATA_LOAD, params);
  }

  private void startReloadTimeoutTimer(Params params) {
    timerId = softTimer.startTimer(EventType.CHEATING_DATA_LOAD_TIMEOUT, params, loadTimeout);
  }

  private void cancelReloadTimeoutTimer() {
    softTimer.cancel(timerId);
  }

  private void startNextReloadTimer(Params params) {
    softTimer.startTimer(EventType.CHEATING_DATA_LOAD, params, reloadInterval);
  }

  public void switchState(State state) {
    this.currentState = state;
  }

  public void handleEvent(EventType eventType, Params params) {
    switch (eventType) {
      case CHEATING_DATA_LOAD:
        handleReload(params);
        break;
      case CHEATING_DATA_LOAD_RESPONSE:
        handleResponse(params);
        break;
      case CHEATING_DATA_LOAD_ERROR:
        handleError(params);
        break;
      case CHEATING_DATA_LOAD_TIMEOUT:
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
            List<CheatingData> total = new ArrayList<>();
            List<CheatingData> cheatingData;
            Integer hashCode = 0;
            do {
              cheatingData = mapper.getCheatingData(hashCode, batchSize);
              hashCode = cheatingData.stream()
                                     .map(CheatingData::getHashCode)
                                     .max(Integer::compareTo)
                                     .orElse(Integer.MAX_VALUE);
              total.addAll(cheatingData);
            } while (cheatingData.size() > 0);
            Map<String, List<CheatingData>> collect =
              total.stream().collect(Collectors.groupingBy(CheatingData::getType));
            List<CheatingData> ipData = collect.get("IP");
            List<CheatingData> deviceData = collect.get("DEVICE_ID");
            BloomFilter ipFilter =
              BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), ipData.size(), fpp);
            BloomFilter deviceIdFilter =
              BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), deviceData.size(), fpp);
            for (CheatingData data : ipData) {
              ipFilter.put(data.getCheatKey());
            }
            for (CheatingData data : deviceData) {
              deviceIdFilter.put(data.getCheatKey());
            }
            params.put(ParamKey.CHEATING_DATA_CACHE_KEY_IP, ipFilter);
            params.put(ParamKey.CHEATING_DATA_CACHE_KEY_DID, deviceIdFilter);
            messageQueue.putMessage(EventType.CHEATING_DATA_LOAD_RESPONSE, params);
          } catch (Exception e) {
            log.error("cheating data load failure from db", e);
            messageQueue.putMessage(EventType.CHEATING_DATA_LOAD_ERROR, params);
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
        cancelReloadTimeoutTimer();
        this.ipFilter = params.get(ParamKey.CHEATING_DATA_CACHE_KEY_IP);
        this.deviceIdFilter = params.get(ParamKey.CHEATING_DATA_CACHE_KEY_DID);
        log.info("cheating data load success");
        startNextReloadTimer(params);
        switchState(State.RUNNING);
        break;
      case UPDATING:
        cancelReloadTimeoutTimer();
        this.ipFilter = params.get(ParamKey.CHEATING_DATA_CACHE_KEY_IP);
        this.deviceIdFilter = params.get(ParamKey.CHEATING_DATA_CACHE_KEY_DID);
        log.info("cheating data load success");
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
        log.error("timeout load cheating data");
        startNextReloadTimer(params);
        switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
        break;
      default:
        log.error("Can't handle event, state: {}", currentState);
    }
  }

}
