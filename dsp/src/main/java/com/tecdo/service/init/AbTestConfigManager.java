package com.tecdo.service.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.entity.AbTestConfig;
import com.tecdo.mapper.AbTestConfigMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Zeki on 2022/12/27
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class AbTestConfigManager extends ServiceImpl<AbTestConfigMapper, AbTestConfig> {

  private final SoftTimer softTimer;
  private final MessageQueue messageQueue;
  private final ThreadPool threadPool;

  private State currentState = State.INIT;
  private long timerId;

  private Map<String, List<AbTestConfig>> abTestConfigMap;

  @Value("${pac.timeout.load.db.default}")
  private long loadTimeout;
  @Value("${pac.interval.reload.db.default}")
  private long reloadInterval;

  /**
   * 从 DB 加载 ab test config 集合，每 5 分钟刷新一次缓存
   */
  public Map<String, List<AbTestConfig>> getAbTestConfigMap() {
    return this.abTestConfigMap;
  }

  public List<AbTestConfig> getAbTestConfigList(String tag) {
    return abTestConfigMap.get(tag);
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
    messageQueue.putMessage(EventType.AB_TEST_CONFIG_LOAD, params);
  }

  private void startReloadTimeoutTimer(Params params) {
    timerId = softTimer.startTimer(EventType.AB_TEST_CONFIG_LOAD_TIMEOUT, params, loadTimeout);
  }

  private void cancelReloadTimeoutTimer() {
    softTimer.cancel(timerId);
  }

  private void startNextReloadTimer(Params params) {
    softTimer.startTimer(EventType.AB_TEST_CONFIG_LOAD, params, reloadInterval);
  }

  public void switchState(State state) {
    this.currentState = state;
  }

  public void handleEvent(EventType eventType, Params params) {
    switch (eventType) {
      case AB_TEST_CONFIG_LOAD:
        handleAbTestConfigReload(params);
        break;
      case AB_TEST_CONFIG_LOAD_RESPONSE:
        handleAbTestConfigResponse(params);
        break;
      case AB_TEST_CONFIG_LOAD_ERROR:
        handleAbTestConfigError(params);
        break;
      case AB_TEST_CONFIG_LOAD_TIMEOUT:
        handleAbTestConfigTimeout(params);
        break;
      default:
        log.error("Can't handle event, type: {}", eventType);
    }
  }

  private void handleAbTestConfigReload(Params params) {
    switch (currentState) {
      case INIT:
      case RUNNING:
        threadPool.execute(() -> {
          try {
            LambdaQueryWrapper<AbTestConfig> wrapper =
              Wrappers.<AbTestConfig>lambdaQuery().eq(AbTestConfig::getStatus, 1);
            Map<String, List<AbTestConfig>> abTestConfig =
              list(wrapper).stream().collect(Collectors.groupingBy(AbTestConfig::getGroup));
            params.put(ParamKey.AB_TEST_CONFIG_CACHE_KEY, abTestConfig);
            messageQueue.putMessage(EventType.AB_TEST_CONFIG_LOAD_RESPONSE, params);
          } catch (Exception e) {
            log.error("ab test config load failure from db", e);
            messageQueue.putMessage(EventType.AB_TEST_CONFIG_LOAD_ERROR, params);
          }
        });
        startReloadTimeoutTimer(params);
        switchState(currentState == State.INIT ? State.WAIT_INIT_RESPONSE : State.UPDATING);
        break;
      default:
        log.error("Can't handle event, state: {}", currentState);
    }
  }

  private void handleAbTestConfigResponse(Params params) {
    switch (currentState) {
      case WAIT_INIT_RESPONSE:
        messageQueue.putMessage(EventType.ONE_DATA_READY);
      case UPDATING:
        cancelReloadTimeoutTimer();
        this.abTestConfigMap = params.get(ParamKey.AB_TEST_CONFIG_CACHE_KEY);
        log.info("ab test config load success, size: {}", abTestConfigMap.size());
        startNextReloadTimer(params);
        switchState(State.RUNNING);
        break;
      default:
        log.error("Can't handle event, state: {}", currentState);
    }
  }

  private void handleAbTestConfigError(Params params) {
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

  private void handleAbTestConfigTimeout(Params params) {
    switch (currentState) {
      case WAIT_INIT_RESPONSE:
      case UPDATING:
        log.error("timeout load ab test config");
        startNextReloadTimer(params);
        switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
        break;
      default:
        log.error("Can't handle event, state: {}", currentState);
    }
  }

}
