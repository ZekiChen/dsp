package com.tecdo.job.service;

import com.tecdo.common.constant.HttpCode;
import com.tecdo.common.util.Params;
import com.tecdo.job.constant.EventType;
import com.tecdo.job.constant.ParamKey;
import com.tecdo.job.controller.MessageQueue;
import com.tecdo.job.server.NetServer;
import com.tecdo.job.server.handler.SimpleHttpChannelInboundHandler;
import com.tecdo.job.server.request.HttpRequest;
import com.tecdo.job.service.init.BudgetManager;
import com.tecdo.job.service.init.CampaignManager;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by Zeki on 2022/12/27
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class LifeCycleManager {

  private final BudgetManager budgetManager;
  private final CampaignManager campaignManager;

  private final MessageQueue messageQueue;

  private State currentState = State.INIT;

  private int readyCount = 0;
  private final int needInitCount = 2;

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
      case BUDGETS_LOAD:
      case BUDGETS_LOAD_RESPONSE:
      case BUDGETS_LOAD_ERROR:
      case BUDGETS_LOAD_TIMEOUT:
        budgetManager.handleEvent(eventType, params);
        break;
      case CAMPAIGNS_LOAD:
      case CAMPAIGNS_LOAD_RESPONSE:
      case CAMPAIGNS_LOAD_ERROR:
      case CAMPAIGNS_LOAD_TIMEOUT:
        campaignManager.handleEvent(eventType, params);
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
        budgetManager.init(params);
        campaignManager.init(params);
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
