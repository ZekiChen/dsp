package com.tecdo.controller;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.fsm.ContextManager;
import com.tecdo.service.LifeCycleManager;
import com.tecdo.service.NoticeService;
import com.tecdo.service.SDKNoticeService;
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
  @Autowired
  private NoticeService noticeService;
  @Autowired
  private SDKNoticeService sdkNoticeService;


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
      case BUDGETS_LOAD:
      case BUDGETS_LOAD_RESPONSE:
      case BUDGETS_LOAD_ERROR:
      case BUDGETS_LOAD_TIMEOUT:
      case GP_APP_LOAD:
      case GP_APP_LOAD_RESPONSE:
      case GP_APP_LOAD_ERROR:
      case GP_APP_LOAD_TIMEOUT:
      case AB_TEST_CONFIG_LOAD:
      case AB_TEST_CONFIG_LOAD_RESPONSE:
      case AB_TEST_CONFIG_LOAD_ERROR:
      case AB_TEST_CONFIG_LOAD_TIMEOUT:
      case IP_TABLE_LOAD:
      case IP_TABLE_LOAD_RESPONSE:
      case IP_TABLE_LOAD_ERROR:
      case IP_TABLE_LOAD_TIMEOUT:
      case AF_AUDIENCE_SYNC_TABLE_LOAD:
      case AF_AUDIENCE_SYNC_LOAD_RESPONSE:
      case AF_AUDIENCE_SYNC_LOAD_ERROR:
      case AF_AUDIENCE_SYNC_LOAD_TIMEOUT:
      case AFF_COUNTRY_BUNDLE_LIST_LOAD:
      case AFF_COUNTRY_BUNDLE_LIST_LOAD_RESPONSE:
      case AFF_COUNTRY_BUNDLE_LIST_LOAD_ERROR:
      case AFF_COUNTRY_BUNDLE_LIST_LOAD_TIMEOUT:
      case BUNDLE_DATA_LOAD:
      case BUNDLE_DATA_LOAD_RESPONSE:
      case BUNDLE_DATA_LOAD_ERROR:
      case BUNDLE_DATA_LOAD_TIMEOUT:
      case CHEATING_DATA_LOAD:
      case CHEATING_DATA_LOAD_RESPONSE:
      case CHEATING_DATA_LOAD_ERROR:
      case CHEATING_DATA_LOAD_TIMEOUT:
      case ECPX_LOAD:
      case ECPX_LOAD_RESPONSE:
      case ECPX_LOAD_ERROR:
      case ECPX_LOAD_TIMEOUT:
      case ONE_DATA_READY:
      case NETTY_START:
      case RECEIVE_PING_REQUEST:
        lifeCycleManager.handleEvent(eventType, params);
        break;
      case VALIDATE_BID_REQUEST:
        validateService.validateBidRequest(params.get(ParamKey.HTTP_REQUEST));
        break;
      // notice request
      case RECEIVE_WIN_NOTICE:
      case RECEIVE_IMP_NOTICE:
      case RECEIVE_CLICK_NOTICE:
      case RECEIVE_PB_NOTICE:
      case RECEIVE_IMP_INFO_NOTICE:
      case RECEIVE_LOSS_NOTICE:
        noticeService.handleEvent(eventType, params);
        break;
      case RECEIVE_SDK_PB_NOTICE:
        sdkNoticeService.handelEvent(eventType, params);
        break;
      // context
      case RECEIVE_BID_REQUEST:
      case BID_TASK_FINISH:
      case BID_TASK_FAILED:
      case WAIT_TASK_RESPONSE_TIMEOUT:
      case DISTINCT_AD_RESPONSE:
      case DISTINCT_AD_TIMEOUT:
      case BID_REQUEST_COMPLETE:
      // task
      case TASK_START:
      case ADS_RECALL_FINISH:
      case ADS_RECALL_ERROR:
      case ADS_RECALL_TIMEOUT:
      case PREDICT_FINISH:
      case PREDICT_ERROR:
      case PREDICT_TIMEOUT:
      case CALC_CPC_FINISH:
      case CALC_CPC_ERROR:
      case CALC_CPC_TIMEOUT:
      case PRICE_FILTER_FINISH:
      case PRICE_FILTER_TIMEOUT:
      case REQUEST_RTA_RESPONSE:
      case WAIT_REQUEST_RTA_RESPONSE_TIMEOUT:
      case WAIT_REQUEST_RTA_RESPONSE_ERROR:
      case SORT_AD_RESPONSE:
      case WAIT_SORT_AD_TIMEOUT:
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
