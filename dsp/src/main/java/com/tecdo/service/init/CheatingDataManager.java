package com.tecdo.service.init;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.core.launch.thread.ThreadPool;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import cn.hutool.core.date.DateUtil;
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

  private Map<String, BloomFilter<CharSequence>> filterMap;

  @Value("${pac.cheating.filter-name-list:}")
  private String enableFilter;

  @Value("${pac.cheating.base-dir:~/data/}")
  private String baseDir;

  /**
   * TODO
   *  数据边查询边插入到布隆过滤器，减少内存占用；需要先查询每个reason的数据来初始化过滤器；
   *  按照reason来区分布隆过滤器；需要根据reason保留是否启用过滤的配置
   */

  @Value("${pac.timeout.load.cheating.data:60000}")
  private long loadTimeout;
  @Value("${pac.interval.reload.cheating.data:1800000}")
  private long reloadInterval;

  public boolean contains(String filterName, String key) {
    return Optional.ofNullable(filterMap.get(filterName))
                   .map(i -> i.mightContain(key))
                   .orElse(true);

  }

  public Pair<Boolean, String> check(String key) {
    for (Map.Entry<String, BloomFilter<CharSequence>> entry : filterMap.entrySet()) {
      String reason = entry.getKey();
      BloomFilter<CharSequence> filter = entry.getValue();
      if (filter.mightContain(key)) {
        return Pair.of(true, reason);
      }
    }
    return Pair.of(false, null);
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
            Map<String, BloomFilter<CharSequence>> filterMap = new HashMap<>();
            if (StringUtils.isEmpty(enableFilter)) {
              params.put(ParamKey.CHEATING_DATA_FILTER, filterMap);
              messageQueue.putMessage(EventType.CHEATING_DATA_LOAD_RESPONSE, params);
              return;
            }
            // 按照配置启用的过滤器从磁盘中加载持久化的过滤器
            // 默认加载当前的过滤器，如果不存在，则加载昨天的过滤器,如果昨天也没有这个过滤器，则跳过
            for (String filterName : enableFilter.split(",")) {
              Calendar calendar = Calendar.getInstance();
              String fileName =
                baseDir + DateUtil.format(calendar.getTime(), "yyyyMMdd") + "/" + filterName;
              File f = new File(fileName);
              BloomFilter<CharSequence> filter = null;
              if (f.exists()) {
                InputStream in = new FileInputStream(f);
                filter = BloomFilter.readFrom(in, Funnels.stringFunnel(Charsets.UTF_8));
              } else {
                calendar.add(Calendar.DATE, -1);
                fileName =
                  baseDir + DateUtil.format(calendar.getTime(), "yyyyMMdd") + "/" + filterName;
                f = new File(fileName);
                if (f.exists()) {
                  InputStream in = new FileInputStream(f);
                  filter = BloomFilter.readFrom(in, Funnels.stringFunnel(Charsets.UTF_8));
                }
              }
              if (filter != null) {
                filterMap.put(filterName, filter);
              }
            }
            params.put(ParamKey.CHEATING_DATA_FILTER, filterMap);
            messageQueue.putMessage(EventType.CHEATING_DATA_LOAD_RESPONSE, params);
          } catch (Exception e) {
            log.error("cheating data load failure from volume", e);
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
        this.filterMap = params.get(ParamKey.CHEATING_DATA_FILTER);
        log.info("cheating data load success");
        startNextReloadTimer(params);
        switchState(State.RUNNING);
        break;
      case UPDATING:
        cancelReloadTimeoutTimer();
        this.filterMap = params.get(ParamKey.CHEATING_DATA_FILTER);
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
