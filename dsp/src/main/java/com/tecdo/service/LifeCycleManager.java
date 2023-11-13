package com.tecdo.service;

import com.tecdo.common.constant.HttpCode;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.log.NotBidReasonLogger;
import com.tecdo.server.NetServer;
import com.tecdo.server.handler.SimpleHttpChannelInboundHandler;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.service.init.AbTestConfigManager;
import com.tecdo.service.init.AdManager;
import com.tecdo.service.init.AfAudienceSyncManager;
import com.tecdo.service.init.AffCountryBundleListManager;
import com.tecdo.service.init.AffiliateManager;
import com.tecdo.service.init.doris.BundleDataManager;
import com.tecdo.service.init.CheatingDataManager;
import com.tecdo.service.init.doris.GooglePlayAppManager;
import com.tecdo.service.init.IpTableManager;
import com.tecdo.service.init.RtaInfoManager;
import com.tecdo.service.init.doris.BundleCostManager;
import com.tecdo.service.init.doris.ECPXManager;
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
  private MessageQueue messageQueue;
  @Autowired
  private AfAudienceSyncManager afAudienceSyncManager;
  @Autowired
  private AffCountryBundleListManager affCountryBundleListManager;
  @Autowired
  private BundleDataManager bundleDataManager;
  @Autowired
  private CheatingDataManager cheatingDataManager;
  @Autowired
  private ECPXManager eCPXManager;
  @Autowired
  private BundleCostManager bundleCostManager;
  private State currentState = State.INIT;

  private int readyCount = 0;
  private final int needInitCount = 13;

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
      case AFF_COUNTRY_BUNDLE_LIST_LOAD:
      case AFF_COUNTRY_BUNDLE_LIST_LOAD_RESPONSE:
      case AFF_COUNTRY_BUNDLE_LIST_LOAD_ERROR:
      case AFF_COUNTRY_BUNDLE_LIST_LOAD_TIMEOUT:
        affCountryBundleListManager.handleEvent(eventType, params);
        break;
      case BUNDLE_DATA_LOAD:
      case BUNDLE_DATA_LOAD_RESPONSE:
      case BUNDLE_DATA_LOAD_ERROR:
      case BUNDLE_DATA_LOAD_TIMEOUT:
        bundleDataManager.handleEvent(eventType, params);
        break;
      case CHEATING_DATA_LOAD:
      case CHEATING_DATA_LOAD_RESPONSE:
      case CHEATING_DATA_LOAD_ERROR:
      case CHEATING_DATA_LOAD_TIMEOUT:
        cheatingDataManager.handleEvent(eventType, params);
        break;
      case ECPX_LOAD:
      case ECPX_LOAD_RESPONSE:
      case ECPX_LOAD_ERROR:
      case ECPX_LOAD_TIMEOUT:
        eCPXManager.handleEvent(eventType, params);
        break;
      case BUNDLE_COST_LOAD:
      case BUNDLE_COST_LOAD_RESPONSE:
      case BUNDLE_COST_LOAD_ERROR:
      case BUNDLE_COST_LOAD_TIMEOUT:
        bundleCostManager.handleEvent(eventType, params);
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
        affCountryBundleListManager.init(params);
        bundleDataManager.init(params);
        cheatingDataManager.init(params);
        eCPXManager.init(params);
        bundleCostManager.init(params);
        NotBidReasonLogger.init();
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
