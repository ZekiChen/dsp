package com.tecdo.service;

import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.constant.RequestKeyByForce;
import com.tecdo.controller.MessageQueue;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.enums.biz.NotForceReasonEnum;
import com.tecdo.log.NotForceLogger;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.service.init.AffiliateManager;
import com.tecdo.util.ResponseHelper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * Created by Zeki on 2023/11/22
 */
@Service
@RequiredArgsConstructor
public class ForceService {

    private final MessageQueue messageQueue;
    private final CacheService cacheService;
    private final ValidateService validateService;
    private final ThreadPool threadPool;
    private final AffiliateManager affiliateManager;

    @Value("${pac.force.affiliate.ip-check:true}")
    private boolean ipCheckEnabled;

    public void handelEvent(EventType eventType, Params params) {
        switch (eventType) {
            case RECEIVE_FORCE_REQUEST:
            default:
                handle(params, params.get(ParamKey.HTTP_REQUEST));
                break;
        }
    }

    /**
     * 首次曝光 和 ip 校验
     */
    private void handle(Params params, HttpRequest httpRequest) {
        threadPool.execute(() -> {
            String bidId = httpRequest.getParamAsStr(RequestKeyByForce.BID_ID);
            String ip = httpRequest.getParamAsStr(RequestKeyByForce.IP);
            Integer affId = httpRequest.getParamAsInteger(RequestKeyByForce.AFFILIATE_ID);
            Affiliate affiliate = affiliateManager.getAffiliate(affId);
            if (StrUtil.hasBlank(bidId, ip, httpRequest.getIp())) {
                NotForceLogger.log(httpRequest, NotForceReasonEnum.PARAM_MISS.getCode());
                ResponseHelper.notForceJump(messageQueue, params, httpRequest);
            } else if (ipCheckEnabled && affiliate.getIpCheckEnabled()
                    && !isIpMatchForFirstThree(ip, httpRequest.getIp())) {
                NotForceLogger.log(httpRequest, NotForceReasonEnum.IP_NOT_MATCH.getCode());
                ResponseHelper.notForceJump(messageQueue, params, httpRequest);
            } else {  // jump
                NotForceLogger.log(httpRequest, NotForceReasonEnum.SUCCESS.getCode());
                ResponseHelper.forceJump(messageQueue, params, httpRequest);
            }
        });
    }

    private boolean isIpMatchForFirstThree(String respIp, String impIp) {
        String[] respIpByDot = respIp.split("\\.");
        String[] impIpByDot = impIp.split("\\.");
        if (respIpByDot.length != 4 || impIpByDot.length != 4) {
            return true;
        }
        return respIpByDot[0].equals(impIpByDot[0])
                && respIpByDot[1].equals(impIpByDot[1])
                && respIpByDot[2].equals(impIpByDot[2]);
    }
}
