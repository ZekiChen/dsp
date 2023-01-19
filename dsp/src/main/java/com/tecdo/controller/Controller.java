package com.tecdo.controller;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.fsm.ContextManager;
import com.tecdo.service.LifeCycleManager;
import com.tecdo.service.ValidateService;
import com.tecdo.util.HttpResponseHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Zeki on 2022/12/27
 **/
@Slf4j
@Component
public class Controller implements MessageObserver {

  @Autowired
  private LifeCycleManager lifeCycleManager;
  @Autowired
  private ContextManager contextManager;
  @Autowired
  private ValidateService validateService;


  @Override
  public void handle(EventType eventType, Params params) {
    switch (eventType) {
      case SERVER_START:
      case AFFILIATES_LOAD:
      case AFFILIATES_LOAD_RESPONSE:
      case AFFILIATES_LOAD_ERROR:
      case AFFILIATES_LOAD_TIMEOUT:
      case ADS_LOAD:
      case ADS_LOAD_RESPONSE:
      case ADS_LOAD_ERROR:
      case ADS_LOAD_TIMEOUT:
      case RTA_INFOS_LOAD:
      case RTA_INFOS_LOAD_RESPONSE:
      case RTA_INFOS_LOAD_ERROR:
      case RTA_INFOS_LOAD_TIMEOUT:
      case ONE_DATA_READY:
      case NETTY_START:
        lifeCycleManager.handleEvent(eventType, params);
        break;
      case VALIDATE_BID_REQUEST:
        validateService.validateBidRequest(params.get(ParamKey.HTTP_REQUEST));
        break;
      case RECEIVE_BID_REQUEST:
      case BID_TASK_FINISH:
      case BID_TASK_FAILED:
      case WAIT_TASK_RESPONSE_TIMEOUT:
      case REQUEST_RTA_RESPONSE:
      case WAIT_REQUEST_RTA_RESPONSE_TIMEOUT:
      case WAIT_REQUEST_RTA_RESPONSE_ERROR:
      case SORT_AD_RESPONSE:
      case WAIT_SORT_AD_TIMEOUT:
      case BID_REQUEST_COMPLETE:
        // task
      case TASK_START:
      case ADS_RECALL_FINISH:
      case ADS_RECALL_ERROR:
      case ADS_RECALL_TIMEOUT:
      case CTR_PREDICT_FINISH:
      case CTR_PREDICT_ERROR:
      case CTR_PREDICT_TIMEOUT:
      case CALC_CPC_FINISH:
      case CALC_CPC_ERROR:
      case CALC_CPC_TIMEOUT:
        contextManager.handleEvent(eventType, params);
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
