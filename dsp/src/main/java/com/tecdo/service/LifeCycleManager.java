package com.tecdo.service;

import com.tecdo.common.constant.HttpCode;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.server.NetServer;
import com.tecdo.server.handler.SimpleHttpChannelInboundHandler;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.service.init.AbTestConfigManager;
import com.tecdo.service.init.AdManager;
import com.tecdo.service.init.AdvManager;
import com.tecdo.service.init.AffiliateManager;
import com.tecdo.service.init.GooglePlayAppManager;
import com.tecdo.service.init.IpTableManager;
import com.tecdo.service.init.RtaInfoManager;
import com.tecdo.service.init.doris.BudgetManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Zeki on 2022/12/27
 **/
@Slf4j
@Component
public class LifeCycleManager {

  @Autowired
  private AffiliateManager affManager;
  @Autowired
  private AbTestConfigManager abTestConfigManager;
  @Autowired
  private AdManager adManager;
  @Autowired
  private RtaInfoManager rtaManager;
  @Autowired
  private BudgetManager budgetManager;
  @Autowired
  private GooglePlayAppManager googlePlayAppManager;
  @Autowired
  private IpTableManager ipTableManager;
  @Autowired
  private AdvManager advManager;
  @Autowired
  private MessageQueue messageQueue;
  @Autowired
  private AfAudienceSyncManager afAudienceSyncManager;

  private State currentState = State.INIT;

  private int readyCount = 0;
  private final int needInitCount = 9;

  @Value("${server.port}")
  private int serverPort;

  @AllArgsConstructor
  private enum State {
    INIT(1, "init"),
    WAIT_DATA_INIT_COMPLETED(2, "waiting data init completed"),
    RUNNING(3, "data init success, now is running");

    private int code;
    private String desc;

    @Override
    public String toString() {
      return code + " - " + desc;
    }
  }

  private void switchState(State state) {
    currentState = state;
  }

  public void handleEvent(EventType eventType, Params params) {
    switch (eventType) {
      case SERVER_START:
        handleDbDataInit();
        break;
      case AFFILIATES_LOAD:
      case AFFILIATES_LOAD_RESPONSE:
      case AFFILIATES_LOAD_ERROR:
      case AFFILIATES_LOAD_TIMEOUT:
        affManager.handleEvent(eventType, params);
        break;
      case AF_AUDIENCE_SYNC_TABLE_LOAD:
      case AF_AUDIENCE_SYNC_LOAD_RESPONSE:
      case AF_AUDIENCE_SYNC_LOAD_ERROR:
      case AF_AUDIENCE_SYNC_LOAD_TIMEOUT:
        afAudienceSyncManager.handleEvent(eventType, params);
        break;
      case ADS_LOAD:
      case ADS_LOAD_RESPONSE:
      case ADS_LOAD_ERROR:
      case ADS_LOAD_TIMEOUT:
        adManager.handleEvent(eventType, params);
        break;
      case RTA_INFOS_LOAD:
      case RTA_INFOS_LOAD_RESPONSE:
      case RTA_INFOS_LOAD_ERROR:
      case RTA_INFOS_LOAD_TIMEOUT:
        rtaManager.handleEvent(eventType, params);
        break;
      case BUDGETS_LOAD:
      case BUDGETS_LOAD_RESPONSE:
      case BUDGETS_LOAD_ERROR:
      case BUDGETS_LOAD_TIMEOUT:
        budgetManager.handleEvent(eventType, params);
        break;
      case GP_APP_LOAD:
      case GP_APP_LOAD_RESPONSE:
      case GP_APP_LOAD_ERROR:
      case GP_APP_LOAD_TIMEOUT:
        googlePlayAppManager.handleEvent(eventType, params);
        break;
      case AB_TEST_CONFIG_LOAD:
      case AB_TEST_CONFIG_LOAD_RESPONSE:
      case AB_TEST_CONFIG_LOAD_ERROR:
      case AB_TEST_CONFIG_LOAD_TIMEOUT:
        abTestConfigManager.handleEvent(eventType, params);
        break;
      case IP_TABLE_LOAD:
      case IP_TABLE_LOAD_RESPONSE:
      case IP_TABLE_LOAD_ERROR:
      case IP_TABLE_LOAD_TIMEOUT:
        ipTableManager.handleEvent(eventType, params);
        break;
      case ADV_LOAD:
      case ADV_LOAD_RESPONSE:
      case ADV_LOAD_ERROR:
      case ADV_LOAD_TIMEOUT:
        advManager.handleEvent(eventType, params);
        break;
      case ONE_DATA_READY:
        handleFinishDbDataInit();
        break;
      case NETTY_START:
        handleNettyStart();
        break;
      case RECEIVE_PING_REQUEST:
        handlePingRequest(params);
        break;
      default:
        log.error("Can't handle event, type: {}", eventType);
    }
  }

  private void handleDbDataInit() {
    switch (currentState) {
      case INIT:
        Params params = Params.create();
        affManager.init(params);
        adManager.init(params);
        rtaManager.init(params);
        budgetManager.init(params);
        abTestConfigManager.init(params);
        googlePlayAppManager.init(params);
        ipTableManager.init(params);
        afAudienceSyncManager.init(params);
        advManager.init(params);
        switchState(State.WAIT_DATA_INIT_COMPLETED);
        break;
      default:
        log.error("Can't handle event, state: {}", currentState);
    }
  }

  private void handleFinishDbDataInit() {
    switch (currentState) {
      case WAIT_DATA_INIT_COMPLETED:
        if (++readyCount == needInitCount) {
          messageQueue.putMessage(EventType.NETTY_START);
          log.info("DB data init finish!");
          switchState(State.RUNNING);
        }
        break;
      default:
        log.error("Can't handle event, state: {}", currentState);
    }
  }

  private void handleNettyStart() {
    NetServer server = new NetServer();
    server.startup(serverPort, new SimpleHttpChannelInboundHandler(messageQueue));
  }

  private void handlePingRequest(Params params) {
    switch (currentState) {
      case RUNNING:
        HttpRequest httpRequest = params.get(ParamKey.HTTP_REQUEST);
        messageQueue.putMessage(EventType.RESPONSE_RESULT,
                                Params.create(ParamKey.HTTP_CODE, HttpCode.OK)
                                      .put(ParamKey.CHANNEL_CONTEXT,
                                           httpRequest.getChannelContext()));
        break;
      default:
        log.error("Can't handle event, state: {}", currentState);
    }
  }
}
