package com.tecdo.service;

import com.google.common.net.HttpHeaders;
import com.tecdo.adm.api.delivery.dto.CampaignDTO;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.constant.RequestKey;
import com.tecdo.constant.RequestPath;
import com.tecdo.controller.MessageQueue;
import com.tecdo.domain.biz.notice.NoticeInfo;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.service.init.AdManager;
import com.tecdo.service.rta.ae.AePbDataVO;
import com.tecdo.service.rta.ae.AePbInfoVO;
import com.tecdo.util.JsonHelper;
import com.tecdo.util.ResponseHelper;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component

public class NoticeService {

    @Autowired
    private MessageQueue messageQueue;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private ValidateService validateService;
    @Autowired
    private AdManager adManager;

  private final Logger winLog = LoggerFactory.getLogger("win_log");
  private final Logger impLog = LoggerFactory.getLogger("imp_log");
  private final Logger clickLog = LoggerFactory.getLogger("click_log");
  private final Logger pbLog = LoggerFactory.getLogger("pb_log");
  private final Logger validateLog = LoggerFactory.getLogger("validate_notice_log");

    public void handleEvent(EventType eventType, Params params) {
        HttpRequest httpRequest = params.get(ParamKey.HTTP_REQUEST);
        if (RequestPath.PB_AE.equals(httpRequest.getPath())) {
            aePbHandle(eventType, params, httpRequest);
        } else {
            generalHandle(eventType, params, httpRequest);
        }
    }

    private void aePbHandle(EventType eventType, Params params, HttpRequest httpRequest) {
        List<NoticeInfo> noticeInfos = new ArrayList<>();
        AePbDataVO aePbDataVO = JsonHelper.parseObject(httpRequest.getBody(), AePbDataVO.class);
        if (aePbDataVO == null || CollUtil.isEmpty(aePbDataVO.getData())) {
            ResponseHelper.aeParamError(messageQueue, params, httpRequest);
            return;
        }
        for (AePbInfoVO aePbInfoVO : aePbDataVO.getData()) {
            NoticeInfo info = cacheService.getNoticeCache().getNoticeInfo(aePbInfoVO.getBidId());
            if (info == null) {
                ResponseHelper.aeParamError(messageQueue, params, httpRequest);
                return;
            }
            CampaignDTO campaignDTO = adManager.getCampaignDTOMap().get(info.getCampaignId());
            if (campaignDTO == null
                    || campaignDTO.getCampaignRtaInfo() == null
                    || !Objects.equals(campaignDTO.getCampaignRtaInfo().getChannel(), aePbDataVO.getChannel())) {
                ResponseHelper.aeParamError(messageQueue, params, httpRequest);
                return;
            }
            info.setBidId(aePbInfoVO.getBidId());
            info.setSign(aePbInfoVO.getSign());
            info.setUvCnt(aePbInfoVO.getUvCnt());
            info.setMbrCnt(aePbInfoVO.getMbrCnt());
            noticeInfos.add(info);
        }
        List<NoticeInfo> infos = new ArrayList<>();
        for (NoticeInfo info : noticeInfos) {
            ValidateCode code = validateService.validateNoticeRequest(info.getBidId(),
                    info.getSign(), info.getCampaignId(), eventType);
            if (code != ValidateCode.SUCCESS) {
                logValidateFailed(eventType, httpRequest, code, info);
                ResponseHelper.aeParamError(messageQueue, params, httpRequest);
                return;
            }
            info.setValidateCode(code);
            infos.add(info);
        }
        infos.forEach(info -> logValidateSucceed(eventType, httpRequest, info));
        ResponseHelper.aeOK(messageQueue, params, httpRequest);
    }

    private void generalHandle(EventType eventType, Params params, HttpRequest httpRequest) {
        NoticeInfo info = buildInfoFromRequestParam(httpRequest);
        ValidateCode code = validateService.validateNoticeRequest(info.getBidId(),
                info.getSign(), info.getCampaignId(), eventType);
        if (code == ValidateCode.SUCCESS) {
            logValidateSucceed(eventType, httpRequest, info);
            ResponseHelper.ok(messageQueue, params, httpRequest);
        } else {
            logValidateFailed(eventType, httpRequest, code, info);
            ResponseHelper.badRequest(messageQueue, params, httpRequest);
        }
    }

    private static NoticeInfo buildInfoFromRequestParam(HttpRequest httpRequest) {
        NoticeInfo info = new NoticeInfo();
        info.setBidId(httpRequest.getParamAsStr(RequestKey.BID_ID));
        info.setSign(httpRequest.getParamAsStr(RequestKey.SIGN));
        info.setCampaignId(httpRequest.getParamAsInteger(RequestKey.CAMPAIGN_ID));
        info.setAdGroupId(httpRequest.getParamAsInt(RequestKey.AD_GROUP_ID));
        info.setAdId(httpRequest.getParamAsInt(RequestKey.AD_ID));
        info.setCreativeId(httpRequest.getParamAsInt(RequestKey.CREATIVE_ID));
        info.setDeviceId(httpRequest.getParamAsStr(RequestKey.DEVICE_ID));
        info.setEventType(httpRequest.getParamAsStr(RequestKey.EVENT_TYPE));
        info.setBidSuccessPrice(httpRequest.getParamAsStr(RequestKey.BID_SUCCESS_PRICE));
        return info;
    }

    private void logValidateFailed(EventType eventType, HttpRequest httpRequest, ValidateCode code, NoticeInfo noticeInfo) {
        switch (eventType) {
            case RECEIVE_WIN_NOTICE:
                handleValidateFailed("win", noticeInfo, httpRequest, code);
                break;
            case RECEIVE_IMP_NOTICE:
                handleValidateFailed("imp", noticeInfo, httpRequest, code);
                break;
            case RECEIVE_CLICK_NOTICE:
                handleValidateFailed("click", noticeInfo, httpRequest, code);
                break;
            case RECEIVE_PB_NOTICE:
                handleValidateFailed("pb", noticeInfo, httpRequest, code);
                break;
            default:
                log.error("Can't handle event, type: {}, code: {}", eventType, code);
        }
    }

    private void handleValidateFailed(String type, NoticeInfo info,
                                      HttpRequest httpRequest, ValidateCode code) {
        Map<String, Object> map = new HashMap<>();
        map.put("create_time", DateUtil.format(new Date(), "yyyy-MM-dd_HH"));
        map.put("time_millis", System.currentTimeMillis());
        map.put("bid_id", info.getBidId());
        map.put("campaign_id", info.getCampaignId());
        map.put("ad_group_id", info.getAdGroupId());
        map.put("ad_id", info.getAdId());
        map.put("creative_id", info.getCreativeId());
        map.put("device_id", info.getDeviceId());
        map.put("referer", httpRequest.getHeader(HttpHeaders.REFERER));
        map.put("ua", httpRequest.getHeader(HttpHeaders.USER_AGENT));
        map.put("ip", httpRequest.getIp());
        String bidSuccessPrice = info.getBidSuccessPrice();
        map.put("bid_success_price", NumberUtils.isParsable(bidSuccessPrice) ?
                new BigDecimal(bidSuccessPrice).doubleValue() : 0d);
        if (info.getEventType() != null) {
            map.put(info.getEventType(), 1);
        }
        map.put("type", type);
        map.put("code", code.name());

        validateLog.info(JsonHelper.toJSONString(map));
    }

    private void logValidateSucceed(EventType eventType, HttpRequest httpRequest, NoticeInfo noticeInfo) {
        switch (eventType) {
            case RECEIVE_WIN_NOTICE:
                handleWinNotice(httpRequest, noticeInfo);
                break;
            case RECEIVE_IMP_NOTICE:
                handleImpNotice(httpRequest, noticeInfo);
                break;
            case RECEIVE_CLICK_NOTICE:
                handleClickNotice(httpRequest, noticeInfo);
                break;
            case RECEIVE_PB_NOTICE:
                handlePbNotice(httpRequest, noticeInfo);
                break;
            default:
                log.error("Can't handle event, type: {}", eventType);
        }
    }

    private void handleWinNotice(HttpRequest httpRequest, NoticeInfo info) {
        String bidSuccessPrice = info.getBidSuccessPrice();
        Map<String, Object> map = new HashMap<>();
        map.put("create_time", DateUtil.format(new Date(), "yyyy-MM-dd_HH"));
        map.put("time_millis", System.currentTimeMillis());
        map.put("bid_id", info.getBidId());
        map.put("campaign_id", info.getCampaignId());
        map.put("ad_group_id", info.getAdGroupId());
        map.put("ad_id", info.getAdId());
        map.put("creative_id", info.getCreativeId());
        map.put("bid_success_price", NumberUtils.isParsable(bidSuccessPrice) ?
                new BigDecimal(bidSuccessPrice).doubleValue() : 0d);

        winLog.info(JsonHelper.toJSONString(map));
    }

    private void handleImpNotice(HttpRequest httpRequest, NoticeInfo info) {
        Map<String, Object> map = new HashMap<>();
        map.put("create_time", DateUtil.format(new Date(), "yyyy-MM-dd_HH"));
        map.put("time_millis", System.currentTimeMillis());
        map.put("referer", httpRequest.getHeader(HttpHeaders.REFERER));
        map.put("ua_from_imp", httpRequest.getHeader(HttpHeaders.USER_AGENT));
        map.put("bid_id", info.getBidId());
        map.put("ip_from_imp", httpRequest.getIp());
        map.put("campaign_id", info.getCampaignId());
        map.put("ad_group_id", info.getAdGroupId());
        map.put("ad_id", info.getAdId());
        map.put("creative_id", info.getCreativeId());
        map.put("device_id", info.getDeviceId());
        String bidSuccessPrice = info.getBidSuccessPrice();
        map.put("bid_success_price", NumberUtils.isParsable(bidSuccessPrice) ?
                new BigDecimal(bidSuccessPrice).doubleValue() : 0d);

        impLog.info(JsonHelper.toJSONString(map));
        cacheService.getFrequencyCache().incrImpCount(String.valueOf(info.getCampaignId()), info.getDeviceId());
    }

    private void handleClickNotice(HttpRequest httpRequest, NoticeInfo info) {
        Map<String, Object> map = new HashMap<>();
        map.put("create_time", DateUtil.format(new Date(), "yyyy-MM-dd_HH"));
        map.put("time_millis", System.currentTimeMillis());
        map.put("referer", httpRequest.getHeader(HttpHeaders.REFERER));
        map.put("ua_from_click", httpRequest.getHeader(HttpHeaders.USER_AGENT));
        map.put("bid_id", info.getBidId());
        map.put("campaign_id", info.getCampaignId());
        map.put("ad_group_id", info.getAdGroupId());
        map.put("ad_id", info.getAdId());
        map.put("creative_id", info.getCreativeId());
        map.put("device_id", info.getDeviceId());
        map.put("ip_from_click", httpRequest.getIp());

        clickLog.info(JsonHelper.toJSONString(map));
        cacheService.getFrequencyCache().incrClickCount(String.valueOf(info.getCampaignId()), info.getDeviceId());
        cacheService.getNoticeCache().clickMark(info.getBidId());
    }

    private void handlePbNotice(HttpRequest httpRequest, NoticeInfo info) {
        Map<String, Object> map = new HashMap<>();
        map.put("create_time", DateUtil.format(new Date(), "yyyy-MM-dd_HH"));
        map.put("time_millis", System.currentTimeMillis());
        map.put("bid_id", info.getBidId());
        map.put("campaign_id", info.getCampaignId());
        map.put("ad_group_id", info.getAdGroupId());
        map.put("ad_id", info.getAdId());
        map.put("creative_id", info.getCreativeId());
        if (info.getEventType() != null) {
            map.put(info.getEventType(), 1);
        }
        if (Objects.equals(info.getUvCnt(), 1)) {
            map.put(RequestKey.EVENT_4, 1);
        }
        if (Objects.equals(info.getMbrCnt(), 1)) {
            map.put(RequestKey.EVENT_5, 1);
        }

        pbLog.info(JsonHelper.toJSONString(map));
    }

}
