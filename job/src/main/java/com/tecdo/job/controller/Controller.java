package com.tecdo.job.controller;

import com.tecdo.common.util.Params;
import com.tecdo.job.constant.EventType;
import com.tecdo.job.service.LifeCycleManager;
import com.tecdo.job.util.HttpResponseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Zeki on 2022/12/27
 **/
@Slf4j
@Component
public class Controller implements MessageObserver {

  @Autowired
  private LifeCycleManager lifeCycleManager;

  @Override
  public void handle(EventType eventType, Params params) {
    switch (eventType) {
      case SERVER_START:
      case BUDGETS_LOAD:
      case BUDGETS_LOAD_RESPONSE:
      case BUDGETS_LOAD_ERROR:
      case BUDGETS_LOAD_TIMEOUT:
      case CAMPAIGNS_LOAD:
      case CAMPAIGNS_LOAD_RESPONSE:
      case CAMPAIGNS_LOAD_ERROR:
      case CAMPAIGNS_LOAD_TIMEOUT:
      case ONE_DATA_READY:
      case NETTY_START:
      case RECEIVE_PING_REQUEST:
        lifeCycleManager.handleEvent(eventType, params);
        break;
      case RESPONSE_RESULT:
        HttpResponseHelper.reply(params);
        break;
      default:
        log.error("Can't handle event: {} ", eventType);
        break;
    }
  }
}
