package com.tecdo.service;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.HttpCode;
import com.tecdo.constant.ParamKey;
import com.tecdo.constant.RequestKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.util.JsonHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeService {

  private MessageQueue messageQueue;

  private final Logger winLog = LoggerFactory.getLogger("WIN_LOG");
  private final Logger impLog = LoggerFactory.getLogger("IMP_LOG");
  private final Logger clickLog = LoggerFactory.getLogger("CLICK_LOG");
  private final Logger pbLog = LoggerFactory.getLogger("PB_LOG");

  public void handleEvent(EventType eventType, Params params) {
    switch (eventType) {
      case RECEIVE_WIN_NOTICE:
        handleWinNotice(params.get(ParamKey.HTTP_REQUEST));
        break;
      case RECEIVE_IMP_NOTICE:
        handleImpNotice(params.get(ParamKey.HTTP_REQUEST));
        break;
      case RECEIVE_CLICK_NOTICE:
        handleClickNotice(params.get(ParamKey.HTTP_REQUEST));
        break;
      case RECEIVE_PB_NOTICE:
        handlePbNotice(params.get(ParamKey.HTTP_REQUEST));
        break;
      default:
        log.error("Can't handle event, type: {}", eventType);
    }
  }

  private void handleWinNotice(HttpRequest httpRequest) {
    String bidId = httpRequest.getParamAsStr(RequestKey.BID_ID);
    double bidSuccessPrice = httpRequest.getParamAsDouble(RequestKey.BID_SUCCESS_PRICE);
    Map<String, Object> map = new HashMap<>();
    map.put("bid_id", bidId);
    map.put("bid_success_price", bidSuccessPrice);
    winLog.info(JsonHelper.toJSONString(map));

    Params params = Params.create(ParamKey.HTTP_CODE, HttpCode.OK)
                          .put(ParamKey.CHANNEL_CONTEXT, httpRequest.getChannelContext());
    messageQueue.putMessage(EventType.RESPONSE_RESULT, params);
  }

  private void handleImpNotice(HttpRequest httpRequest) {
    String bidId = httpRequest.getParamAsStr(RequestKey.BID_ID);
    double bidSuccessPrice = httpRequest.getParamAsDouble(RequestKey.BID_SUCCESS_PRICE);
    String ipFromImp = httpRequest.getIp();
    int campaignId = httpRequest.getParamAsInt(RequestKey.CAMPAIGN_ID);
    int adGroupId = httpRequest.getParamAsInt(RequestKey.AD_GROUP_ID);
    String createTime = httpRequest.getParamAsStr(RequestKey.CREATE_TIME);
    Map<String, Object> map = new HashMap<>();
    map.put("bid_id", bidId);
    map.put("bid_success_price", bidSuccessPrice);
    map.put("ip_from_imp", ipFromImp);
    map.put("campaign_id", campaignId);
    map.put("ad_group_id", adGroupId);
    map.put("create_time", createTime);
    impLog.info(JsonHelper.toJSONString(map));

    Params params = Params.create(ParamKey.HTTP_CODE, HttpCode.OK)
                          .put(ParamKey.CHANNEL_CONTEXT, httpRequest.getChannelContext());
    messageQueue.putMessage(EventType.RESPONSE_RESULT, params);
  }

  private void handleClickNotice(HttpRequest httpRequest) {
    String bidId = httpRequest.getParamAsStr(RequestKey.BID_ID);
    String ipFromClick = httpRequest.getIp();
    Map<String, Object> map = new HashMap<>();
    map.put("bid_id", bidId);
    map.put("ip_from_click", ipFromClick);
    clickLog.info(JsonHelper.toJSONString(map));

    Params params = Params.create(ParamKey.HTTP_CODE, HttpCode.OK)
                          .put(ParamKey.CHANNEL_CONTEXT, httpRequest.getChannelContext());
    messageQueue.putMessage(EventType.RESPONSE_RESULT, params);
  }

  private void handlePbNotice(HttpRequest httpRequest) {
    String bidId = httpRequest.getParamAsStr(RequestKey.BID_ID);
    String eventType = httpRequest.getParamAsStr(RequestKey.EVENT_TYPE);
    Map<String, Object> map = new HashMap<>();
    map.put("bid_id", bidId);
    map.put(eventType, 1);
    pbLog.info(JsonHelper.toJSONString(map));

    Params params = Params.create(ParamKey.HTTP_CODE, HttpCode.OK)
                          .put(ParamKey.CHANNEL_CONTEXT, httpRequest.getChannelContext());
    messageQueue.putMessage(EventType.RESPONSE_RESULT, params);
  }
}