package com.tecdo.service;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.constant.RequestKeyByForce;
import com.tecdo.controller.MessageQueue;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.enums.biz.NotForceReasonEnum;
import com.tecdo.log.NotForceLogger;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.util.ResponseHelper;

import org.springframework.stereotype.Service;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;

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
            if (StrUtil.hasBlank(bidId, ip)) {
                NotForceLogger.log(httpRequest, NotForceReasonEnum.PARAM_MISS.getCode());
                ResponseHelper.notForceJump(messageQueue, params, httpRequest);
            } else if (!validateService.windowValid(bidId, EventType.RECEIVE_FORCE_REQUEST, affId)) {
                NotForceLogger.log(httpRequest, NotForceReasonEnum.WINDOW_VALID.getCode());
                ResponseHelper.notForceJump(messageQueue, params, httpRequest);
            } else if (!cacheService.getForceCache().impMarkIfAbsent(bidId)) {
                NotForceLogger.log(httpRequest, NotForceReasonEnum.DUPLICATE_VALID.getCode());
                ResponseHelper.notForceJump(messageQueue, params, httpRequest);
            } else {  // jump
                NotForceLogger.log(httpRequest, NotForceReasonEnum.SUCCESS.getCode());
                ResponseHelper.forceJump(messageQueue, params, httpRequest);
            }
        });
    }
}
