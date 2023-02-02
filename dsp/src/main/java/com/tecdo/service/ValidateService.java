package com.tecdo.service;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.HttpCode;
import com.tecdo.constant.ParamKey;
import com.tecdo.constant.RequestKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.Affiliate;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.service.init.AffiliateManager;
import com.tecdo.transform.IProtoTransform;
import com.tecdo.transform.ProtoTransformFactory;

import org.springframework.stereotype.Service;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidateService {

  private final AffiliateManager affiliateManager;

  private final MessageQueue messageQueue;

  public void validateBidRequest(HttpRequest httpRequest) {
    String token = httpRequest.getParamAsStr(RequestKey.TOKEN);
    Affiliate affiliate = affiliateManager.getAffiliate(token);

    if (affiliate == null) {
      messageQueue.putMessage(EventType.RESPONSE_RESULT,
                              Params.create(ParamKey.HTTP_CODE, HttpCode.NOT_FOUND)
                                    .put(ParamKey.CHANNEL_CONTEXT,
                                         httpRequest.getChannelContext()));
      return;
    }
    String api = affiliate.getApi();
    IProtoTransform protoTransform = ProtoTransformFactory.getProtoTransform(api);
    if (protoTransform == null) {
      messageQueue.putMessage(EventType.RESPONSE_RESULT,
                              Params.create(ParamKey.HTTP_CODE, HttpCode.NOT_BID)
                                    .put(ParamKey.CHANNEL_CONTEXT,
                                         httpRequest.getChannelContext()));
      return;
    }
    BidRequest bidRequest = protoTransform.requestTransform(httpRequest.getBody());
    if (bidRequest == null || !validateBidRequest(bidRequest)) {
      messageQueue.putMessage(EventType.RESPONSE_RESULT,
                              Params.create(ParamKey.HTTP_CODE, HttpCode.NOT_BID)
                                    .put(ParamKey.CHANNEL_CONTEXT,
                                         httpRequest.getChannelContext()));
      return;
    }

    messageQueue.putMessage(EventType.RECEIVE_BID_REQUEST,
                            Params.create(ParamKey.BID_REQUEST, bidRequest)
                                  .put(ParamKey.HTTP_REQUEST, httpRequest)
                                  .put(ParamKey.AFFILIATE, affiliate));

  }

  private boolean validateBidRequest(BidRequest bidRequest) {
    // 目标渠道：目前只参与移动端流量的竞价
    if (bidRequest.getApp() == null) {
      return false;
    }
    // 设备信息都不传，不太合理
    if (bidRequest.getDevice() == null) {
      return false;
    }
    // 展示位必须有
    List<Imp> imp = bidRequest.getImp();
    if (CollUtil.isEmpty(imp)) {
      return false;
    }

    return true;
  }

}
