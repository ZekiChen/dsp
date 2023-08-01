package com.tecdo.service;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.constant.RequestKeyBySdk;
import com.tecdo.controller.MessageQueue;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.util.JsonHelper;
import com.tecdo.util.ResponseHelper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SDKNoticeService {

  private static final Logger sdkPbLog = LoggerFactory.getLogger("sdk_pb_log");

  @Autowired
  private MessageQueue messageQueue;

  public void handelEvent(EventType eventType, Params params) {
    switch (eventType) {
      case RECEIVE_SDK_PB_NOTICE:
        handlePb(params, params.get(ParamKey.HTTP_REQUEST));
        break;
      default:
    }
  }

  private void handlePb(Params params, HttpRequest httpRequest) {
    String clickId = httpRequest.getParamAsStr(RequestKeyBySdk.CLICK_ID);
    if (StringUtils.isNotEmpty(clickId) && clickId.length() == 45) {
      Map<String, Object> res = new HashMap<>();
      res.put("click_id", clickId);
      res.put("create_time", DateUtil.format(new Date(), "yyyy-MM-dd_HH"));
      res.put("time_millis", System.currentTimeMillis());
      sdkPbLog.info(JsonHelper.toJSONString(res));
    }
    ResponseHelper.ok(messageQueue, params, httpRequest);
  }

}
