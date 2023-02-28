package com.tecdo.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.tecdo.common.constant.Constant;
import com.tecdo.common.constant.HttpCode;
import com.tecdo.common.util.Params;
import com.tecdo.constant.*;
import com.tecdo.controller.MessageQueue;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.Affiliate;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.service.init.AffiliateManager;
import com.tecdo.transform.IProtoTransform;
import com.tecdo.transform.ProtoTransformFactory;
import com.tecdo.util.SignHelper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ValidateService {

  private final AffiliateManager affiliateManager;

  private final MessageQueue messageQueue;
  private final CacheService cacheService;

  @Value("${pac.notice.expire.win}")
  private long winExpire;

  @Value("${pac.notice.expire.imp}")
  private long impExpire;

  @Value("${pac.notice.expire.click}")
  private long clickExpire;

  @Value("${pac.notice.expire.pb}")
  private long pbExpire;

  public void validateBidRequest(HttpRequest httpRequest) {
    String token = httpRequest.getParamAsStr(RequestKey.TOKEN);
    Affiliate affiliate = affiliateManager.getAffiliate(token);

    if (affiliate == null) {
      messageQueue.putMessage(EventType.RESPONSE_RESULT,
              Params.create(ParamKey.HTTP_CODE, HttpCode.BAD_REQUEST)
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
              Params.create(ParamKey.HTTP_CODE, HttpCode.BAD_REQUEST)
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
    if (bidRequest.getDevice().getIfa() == null ||
        Constant.ERROR_DEVICE_ID.equals(bidRequest.getDevice().getIfa())) {
      return false;
    }
    if (StringUtils.isEmpty(bidRequest.getApp().getBundle())) {
      return false;
    }
    // 展示位必须有
    List<Imp> imp = bidRequest.getImp();
    if (CollUtil.isEmpty(imp)) {
      return false;
    }

    return true;
  }

    /**
     * 通知请求校验：
     * 1. 请求参数校验
     * 2. bid_id 合法性
     * 3. 归因窗口限制
     * 4. click / pb 通知是否来源于上层漏斗
     * 5. bid_id 去重校验
     *
     * @return true: 校验通过，请求放行
     */
    public boolean validateNoticeRequest(HttpRequest httpRequest, EventType eventType) {
        String bidId = httpRequest.getParamAsStr(RequestKey.BID_ID);
        String sign = httpRequest.getParamAsStr(RequestKey.SIGN);
        String campaignId = httpRequest.getParamAsStr(RequestKey.CAMPAIGN_ID);
        if (StrUtil.hasBlank(bidId, sign, campaignId)) {
            return false;
        }
        return bidIdValid(bidId, sign, campaignId)
                && windowValid(bidId, eventType)
                && funnelValid(bidId, eventType)
                && duplicateValid(bidId, eventType);
    }

    private boolean bidIdValid(String bidId, String sign, String campaignId) {
        return Objects.equals(sign, SignHelper.digest(bidId, campaignId));
    }

    private boolean windowValid(String bidId, EventType eventType) {
      long expire = winExpire;
        switch (eventType) {
          case RECEIVE_WIN_NOTICE:
                expire = winExpire;
                break;
          case RECEIVE_IMP_NOTICE:
                expire = impExpire;
                break;
          case RECEIVE_CLICK_NOTICE:
                expire = clickExpire;
                break;
          case RECEIVE_PB_NOTICE:
                expire = pbExpire;
        }
      // bidId 由 32位UUID + 13位时间戳 构成
      if (bidId.length() != 45) {
        return false;
      }
      long createStamp = Long.parseLong(bidId.substring(32, 45));
      long expireStamp = createStamp + (expire * 1000);
      return System.currentTimeMillis() <= expireStamp;
    }

    private boolean funnelValid(String bidId, EventType eventType) {
        switch (eventType) {
            case RECEIVE_CLICK_NOTICE:
                return cacheService.hasWin(bidId) || cacheService.hasImp(bidId);
            case RECEIVE_PB_NOTICE:
                return cacheService.hasClick(bidId);
            default:
                return true;
        }
    }

    private boolean duplicateValid(String bidId, EventType eventType) {
        switch (eventType) {
            case RECEIVE_WIN_NOTICE:
                return cacheService.winMark(bidId);
            case RECEIVE_IMP_NOTICE:
                return cacheService.impMark(bidId);
            default:
                return true;
        }
    }
}
