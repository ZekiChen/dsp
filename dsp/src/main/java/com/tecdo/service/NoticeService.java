package com.tecdo.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.tecdo.adm.api.delivery.dto.CampaignDTO;
import com.tecdo.common.util.Params;
import com.tecdo.constant.*;
import com.tecdo.controller.MessageQueue;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.domain.biz.notice.ImpInfoNoticeInfo;
import com.tecdo.domain.biz.notice.NoticeInfo;
import com.tecdo.log.NoticeLogger;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.service.init.AdManager;
import com.tecdo.service.rta.ae.AePbDataVO;
import com.tecdo.service.rta.ae.AePbInfoVO;
import com.tecdo.util.JsonHelper;
import com.tecdo.util.ResponseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    @Autowired
    private NoticeLogger noticeLogger;
    @Autowired
    private ThreadPool threadPool;

    public void handleEvent(EventType eventType, Params params) {
        HttpRequest httpRequest = params.get(ParamKey.HTTP_REQUEST);
        switch (httpRequest.getPath()) {
            case RequestPath.PB_AE:
                aePbHandle(eventType, params, httpRequest);
                break;
            case RequestPath.IMP_INFO:
                impInfoHandle(eventType, params, httpRequest);
                break;
            default:
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
        log.info("ae pb: {}", httpRequest.getBody());
        for (AePbInfoVO aePbInfoVO : aePbDataVO.getData()) {
            String campaignToCreativeId = aePbInfoVO.getCampaignToCreativeId();
            if (StrUtil.isBlank(campaignToCreativeId)
                    || campaignToCreativeId.split("_").length != 4) {
                ResponseHelper.aeParamError(messageQueue, params, httpRequest);
                return;
            }
            NoticeInfo info = buildAeBaseInfo(campaignToCreativeId);
            CampaignDTO campaignDTO = adManager.getCampaignDTOMap().get(info.getCampaignId());
            if (campaignDTO == null
                    || campaignDTO.getCampaignRtaInfo() == null
                    || !Objects.equals(campaignDTO.getCampaignRtaInfo().getChannel(), aePbDataVO.getChannel())) {
                ResponseHelper.aeParamError(messageQueue, params, httpRequest);
                return;
            }
            info.setDeviceId(aePbInfoVO.getDeviceId());
            info.setAffiliateId(Integer.valueOf(aePbInfoVO.getAffiliateId()));
            info.setBidId(aePbInfoVO.getBidId());
            info.setSign(aePbInfoVO.getSign());
            info.setIsRealtime(aePbInfoVO.getIsRealtime());
            info.setUvCnt(aePbInfoVO.getUvCnt());
            info.setMbrCnt(aePbInfoVO.getMbrCnt());
            info.setOrderAmount(aePbInfoVO.getOrderAmount());
            info.setBuyerCnt(aePbInfoVO.getBuyerCnt());
            info.setP4pRevenue(aePbInfoVO.getP4pRevenue());
            info.setAffiRevenue(aePbInfoVO.getAffiRevenue());
            info.setAddToWishCnt(aePbInfoVO.getAddToWishCnt());
            info.setAddToCartCnt(aePbInfoVO.getAddToCartCnt());

            info.setSessionContentViewList(aePbInfoVO.getSessionContentViewList());
            info.setSessionAddToCartList(aePbInfoVO.getSessionAddToCartList());
            info.setSessionOrderItemList(aePbInfoVO.getSessionOrderItemList());
            info.setFirstContentViewTime(aePbInfoVO.getFirstContentViewTime());
            info.setLastContentViewTime(aePbInfoVO.getLastContentViewTime());
            info.setFirstAddToCartTime(aePbInfoVO.getFirstAddToCartTime());
            info.setLastAddToCartTime(aePbInfoVO.getLastAddToCartTime());
            info.setFirstOrderTime(aePbInfoVO.getFirstOrderTime());
            info.setLastOrderTime(aePbInfoVO.getLastOrderTime());
            noticeInfos.add(info);
        }
        List<NoticeInfo> infos = new ArrayList<>();
        for (NoticeInfo info : noticeInfos) {
            ValidateCode code = validateService.validateNoticeRequest(info.getBidId(),
                    info.getSign(), info.getCampaignId(), eventType, info.getAffiliateId());
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

    private static NoticeInfo buildAeBaseInfo(String campaignToCreativeId) {
        String[] strArr = campaignToCreativeId.split("_");
        NoticeInfo info = new NoticeInfo();
        info.setCampaignId(Integer.valueOf(strArr[0]));
        info.setAdGroupId(Integer.valueOf(strArr[1]));
        info.setAdId(Integer.valueOf(strArr[2]));
        info.setCreativeId(Integer.valueOf(strArr[3]));
        return info;
    }

    private void impInfoHandle(EventType eventType, Params params, HttpRequest httpRequest) {
        ImpInfoNoticeInfo info = ImpInfoNoticeInfo.buildInfo(httpRequest);
        ValidateCode code = validateService.validateNoticeRequest(info.getBidId(),
                info.getSign(), info.getCampaignId(), eventType, info.getAffiliateId());
        if (code == ValidateCode.SUCCESS) {
            logImpInfoValidateSuccess(httpRequest, info);
            ResponseHelper.ok(messageQueue, params, httpRequest);
        } else {
            logValidateFailed(eventType, httpRequest, code, info);
            ResponseHelper.ok(messageQueue, params, httpRequest);
        }
    }

    private void generalHandle(EventType eventType, Params params, HttpRequest httpRequest) {
        NoticeInfo info = NoticeInfo.buildInfo(httpRequest);
        threadPool.execute(() -> {
            try {
                ValidateCode code = validateService.validateNoticeRequest(info.getBidId(),
                        info.getSign(), info.getCampaignId(), eventType, info.getAffiliateId());
                if (code == ValidateCode.SUCCESS) {
                    logValidateSucceed(eventType, httpRequest, info);
                } else {
                    logValidateFailed(eventType, httpRequest, code, info);
                }
            } catch (Exception e) {
                log.error("validate notice request error", e);
            }
        });
        ResponseHelper.ok(messageQueue, params, httpRequest);
    }

    private void logValidateFailed(EventType eventType, HttpRequest httpRequest, ValidateCode code, NoticeInfo noticeInfo) {
        switch (eventType) {
            case RECEIVE_WIN_NOTICE:
                handleValidateFailed("win", noticeInfo, httpRequest, code);
                break;
            case RECEIVE_LOSS_NOTICE:
                handleValidateFailed("loss", noticeInfo, httpRequest, code);
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
            case RECEIVE_IMP_INFO_NOTICE:
                handleValidateFailed("imp-info", noticeInfo, httpRequest, code);
                break;
            default:
                log.error("Can't handle event, type: {}, code: {}", eventType, code);
        }
    }

    private void handleValidateFailed(String type, NoticeInfo info,
                                      HttpRequest httpRequest, ValidateCode code) {
        noticeLogger.logValidateFailed(type, info, httpRequest, code);
    }

    private void logValidateSucceed(EventType eventType, HttpRequest httpRequest, NoticeInfo noticeInfo) {
        switch (eventType) {
            case RECEIVE_WIN_NOTICE:
                handleWinNotice(httpRequest, noticeInfo);
                break;
            case RECEIVE_LOSS_NOTICE:
                handleLossNotice(httpRequest, noticeInfo);
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
        noticeLogger.logWin(httpRequest, info);
    }

    private void handleLossNotice(HttpRequest httpRequest, NoticeInfo info) {
        noticeLogger.logLoss(httpRequest, info);
    }

    private void handleImpNotice(HttpRequest httpRequest, NoticeInfo info) {
        String campaignId = String.valueOf(info.getCampaignId());
        String deviceId = info.getDeviceId();
        noticeLogger.logImp(httpRequest, info);
        cacheService.getFrequencyCache().incrImpCount(campaignId, deviceId);
        cacheService.getFrequencyCache().incrImpCountByHour(campaignId, deviceId);
    }

    private void handleClickNotice(HttpRequest httpRequest, NoticeInfo info) {
        String campaignId = String.valueOf(info.getCampaignId());
        String deviceId = info.getDeviceId();
        noticeLogger.logClick(httpRequest, info);
        cacheService.getFrequencyCache().incrClickCount(campaignId, deviceId);
        cacheService.getFrequencyCache().incrClickCountByHour(campaignId, deviceId);
        cacheService.getNoticeCache().clickMark(info.getBidId());
    }

    private void handlePbNotice(HttpRequest httpRequest, NoticeInfo info) {
        noticeLogger.logPb(httpRequest, info);
    }

    private void logImpInfoValidateSuccess(HttpRequest httpRequest, ImpInfoNoticeInfo info) {
        noticeLogger.logImpInfoValidateSuccess(httpRequest, info);
    }
}
