package com.tecdo.service;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.constant.RequestKeyByCollectInfo;
import com.tecdo.controller.MessageQueue;
import com.tecdo.domain.biz.collect.CollectCode;
import com.tecdo.domain.biz.collect.CollectFeature;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.util.JsonHelper;
import com.tecdo.util.ResponseHelper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CollectService {

  private static final Logger collectFeatureLog = LoggerFactory.getLogger("collect_feature_log");
  private static final Logger collectCodeLog = LoggerFactory.getLogger("collect_code_log");

  private final MessageQueue messageQueue;

  public void handelEvent(EventType eventType, Params params) {
    switch (eventType) {
      case RECEIVE_COLLECT_FEATURE:
        handleCollectFeature(params, params.get(ParamKey.HTTP_REQUEST));
        break;
      case RECEIVE_COLLECT_CODE:
        handelCollectCode(params, params.get(ParamKey.HTTP_REQUEST));
        break;
      default:
        break;
    }
  }

  private void handleCollectFeature(Params params, HttpRequest httpRequest) {
    CollectFeature collectFeature = JsonHelper.parseObject(httpRequest.getBody(), CollectFeature.class);

    collectFeature.setBundle(httpRequest.getParamAsStr(RequestKeyByCollectInfo.BUNDLE));
    collectFeature.setBidId(httpRequest.getParamAsStr(RequestKeyByCollectInfo.BID_ID));
    collectFeature.setSchain(httpRequest.getParamAsStr(RequestKeyByCollectInfo.SCHAIN));
    collectFeature.setDeviceId(httpRequest.getParamAsStr(RequestKeyByCollectInfo.DEVICE_ID));
    collectFeature.setIp(httpRequest.getParamAsStr(RequestKeyByCollectInfo.IP));
    collectFeature.setIpFromImp(httpRequest.getIp());
    collectFeature.setAffiliateId(httpRequest.getParamAsInteger(RequestKeyByCollectInfo.AFFILIATE_ID));
    collectFeature.setAdGroupId(httpRequest.getParamAsInteger(RequestKeyByCollectInfo.AD_GROUP_ID));
    collectFeature.setPhantomJS(collectFeature.getPhantomJSFeatures().stream().anyMatch(i->i));
    collectFeature.setSelenium(collectFeature.getSeleniumFeatures().stream().anyMatch(i->i));
    collectFeatureLog.info(JsonHelper.toJSONString(collectFeature));
    ResponseHelper.ok(messageQueue, params, httpRequest);
  }

  private void handelCollectCode(Params params, HttpRequest httpRequest) {
    CollectCode collectCode = JsonHelper.parseObject(httpRequest.getBody(), CollectCode.class);
    if (collectCode == null || StringUtils.isBlank(collectCode.getCode())) {
      ResponseHelper.ok(messageQueue, params, httpRequest);
      return;
    }
    collectCode.setBundle(httpRequest.getParamAsStr(RequestKeyByCollectInfo.BUNDLE));
    collectCode.setBidId(httpRequest.getParamAsStr(RequestKeyByCollectInfo.BID_ID));
    collectCode.setSchain(httpRequest.getParamAsStr(RequestKeyByCollectInfo.SCHAIN));
    collectCode.setDeviceId(httpRequest.getParamAsStr(RequestKeyByCollectInfo.DEVICE_ID));
    collectCode.setIp(httpRequest.getParamAsStr(RequestKeyByCollectInfo.IP));
    collectCode.setIpFromImp(httpRequest.getIp());
    collectCode.setAffiliateId(httpRequest.getParamAsInteger(RequestKeyByCollectInfo.AFFILIATE_ID));
    collectCode.setAdGroupId(httpRequest.getParamAsInteger(RequestKeyByCollectInfo.AD_GROUP_ID));
    collectCodeLog.info(JsonHelper.toJSONString(collectCode));
    ResponseHelper.ok(messageQueue, params, httpRequest);
  }

}
