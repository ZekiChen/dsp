package com.tecdo.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.common.constant.Constant;
import com.tecdo.common.constant.HttpCode;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.constant.RequestKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Device;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.service.init.AffiliateManager;
import com.tecdo.service.init.IpTableManager;
import com.tecdo.service.init.Pair;
import com.tecdo.transform.IProtoTransform;
import com.tecdo.transform.ProtoTransformFactory;
import com.tecdo.util.FieldFormatHelper;
import com.tecdo.util.JsonHelper;
import com.tecdo.util.SignHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidateService {

  private final AffiliateManager affiliateManager;

  private final IpTableManager ipTableManager;

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

  @Value("${pac.request.validate}")
  private boolean needValidateRequest;

  private final Logger requestValidateLog = LoggerFactory.getLogger("validate_request_log");

  public void validateBidRequest(HttpRequest httpRequest) {
    String token = httpRequest.getParamAsStr(RequestKey.TOKEN);
    Affiliate affiliate = affiliateManager.getAffiliate(token);

    if (affiliate == null) {
      log.warn("validate fail! aff doesn't exist, token: {}", token);
      messageQueue.putMessage(EventType.RESPONSE_RESULT,
              Params.create(ParamKey.HTTP_CODE, HttpCode.BAD_REQUEST)
                      .put(ParamKey.CHANNEL_CONTEXT,
                              httpRequest.getChannelContext()));
      return;
    }
    String api = affiliate.getApi();
    IProtoTransform protoTransform = ProtoTransformFactory.getProtoTransform(api);
    if (protoTransform == null) {
      log.warn("validate fail! bid protocol is not supported, api: {}", api);
      messageQueue.putMessage(EventType.RESPONSE_RESULT,
              Params.create(ParamKey.HTTP_CODE, HttpCode.NOT_BID)
                      .put(ParamKey.CHANNEL_CONTEXT,
                              httpRequest.getChannelContext()));
      return;
    }
    if (StringUtils.isEmpty(httpRequest.getBody())) {
      return;
    }
    BidRequest bidRequest = protoTransform.requestTransform(httpRequest.getBody());
    if (bidRequest == null || !validateBidRequest(bidRequest)) {
      log.warn((bidRequest == null ? "bidRequest is null"
              : "validate bidRequest fail") + ", requestId: {}", httpRequest.getRequestId());
      messageQueue.putMessage(EventType.RESPONSE_RESULT,
              Params.create(ParamKey.HTTP_CODE, HttpCode.BAD_REQUEST)
                      .put(ParamKey.CHANNEL_CONTEXT,
                              httpRequest.getChannelContext()));
      return;
    }

    String ip = bidRequest.getDevice().getIp();
    Pair<Boolean, String> blocked = ipTableManager.ipCheck(ip);
    if (blocked.left) {
      Map<String, Object> map = new HashMap<>();
      Device device = bidRequest.getDevice();
      map.put("create_time", DateUtil.format(new Date(), "yyyy-MM-dd_HH"));
      map.put("time_millis", System.currentTimeMillis());
      map.put("affiliate_id", affiliate.getId());
      map.put("affiliate_name", affiliate.getName());
      map.put("bundle_id", bidRequest.getApp().getBundle());
      map.put("os", FieldFormatHelper.osFormat(device.getOs()));
      map.put("osv", device.getOsv());
      map.put("ip", ip);
      map.put("ua", device.getUa());
      map.put("lang", FieldFormatHelper.languageFormat(device.getLanguage()));
      map.put("device_id", FieldFormatHelper.bundleIdFormat(bidRequest.getApp().getBundle()));
      map.put("device_make", FieldFormatHelper.deviceMakeFormat(device.getMake()));
      map.put("device_model", FieldFormatHelper.deviceModelFormat(device.getModel()));
      map.put("connection_type", device.getConnectiontype());
      map.put("country", FieldFormatHelper.countryFormat(device.getGeo().getCountry()));
      map.put("city", FieldFormatHelper.cityFormat(device.getGeo().getCity()));
      map.put("region", FieldFormatHelper.regionFormat(device.getGeo().getRegion()));
      map.put("screen_width", device.getW());
      map.put("screen_height", device.getH());
      map.put("screen_ppi", device.getPpi());
      map.put("blocked_type", blocked.right);
      requestValidateLog.info(JsonHelper.toJSONString(map));

      if (needValidateRequest) {
        messageQueue.putMessage(EventType.RESPONSE_RESULT,
                                Params.create(ParamKey.HTTP_CODE, HttpCode.NOT_BID)
                                      .put(ParamKey.CHANNEL_CONTEXT,
                                           httpRequest.getChannelContext()));
        return;
      }
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
    // 没有设备id或者设备id非法
    if (bidRequest.getDevice().getIfa() == null ||
        Constant.ERROR_DEVICE_ID.equals(bidRequest.getDevice().getIfa())) {
      return false;
    }
    // 没有国家信息
    if (bidRequest.getDevice().getGeo() == null ||
        bidRequest.getDevice().getGeo().getCountry() == null) {
      return false;
    }
    // 没有bundle信息
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
     * @return ValidateCode
     */
    public ValidateCode validateNoticeRequest(String bidId, String sign, Integer campaignId, EventType eventType) {
        if (StrUtil.hasBlank(bidId, sign) || campaignId == null) {
            return ValidateCode.BLANK_VALID_FAILED;
        }
        if (!bidIdValid(bidId, sign, campaignId.toString())) {
            return ValidateCode.BID_ID_VALID_FAILED;
        }
        if (!windowValid(bidId, eventType)) {
            return ValidateCode.WINDOW_VALID_FAILED;
        }
        if (!funnelValid(bidId, eventType)) {
            return ValidateCode.FUNNEL_VALID_FAILED;
        }
        if (!duplicateValid(bidId, eventType)) {
            return ValidateCode.DUPLICATE_VALID_FAILED;
        }
        return ValidateCode.SUCCESS;
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
          case RECEIVE_IMP_INFO_NOTICE:
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
                return cacheService.getNoticeCache().hasWin(bidId)
                        || cacheService.getNoticeCache().hasImp(bidId);
            case RECEIVE_PB_NOTICE:
                return cacheService.getNoticeCache().hasClick(bidId);
            default:
                return true;
        }
    }

    private boolean duplicateValid(String bidId, EventType eventType) {
        switch (eventType) {
            case RECEIVE_WIN_NOTICE:
                return cacheService.getNoticeCache().winMark(bidId);
            case RECEIVE_IMP_NOTICE:
                return cacheService.getNoticeCache().impMark(bidId);
            default:
                return true;
        }
    }
}
