package com.tecdo.service;

import cn.hutool.core.util.StrUtil;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.constant.RequestKeyByForce;
import com.tecdo.controller.MessageQueue;
import com.tecdo.enums.biz.NoForceReasonEnum;
import com.tecdo.log.NotForceLogger;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.util.ResponseHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        String bidId = httpRequest.getParamAsStr(RequestKeyByForce.BID_ID);
        String ip = httpRequest.getParamAsStr(RequestKeyByForce.IP);
        params.put(ParamKey.RESPONSE_BODY, "0");

        if (StrUtil.hasBlank(bidId, ip)) {
            NotForceLogger.log(httpRequest, NoForceReasonEnum.PARAM_MISS.getCode());
        } else if (!validateService.windowValid(bidId, EventType.RECEIVE_IMP_NOTICE)) {
            NotForceLogger.log(httpRequest, NoForceReasonEnum.WINDOW_VALID.getCode());
        } else if (cacheService.getNoticeCache().hasImp(bidId)) {
            NotForceLogger.log(httpRequest, NoForceReasonEnum.FUNNEL_VALID.getCode());
        }
//        else if (!Objects.equals(ip, httpRequest.getIp())) {
//            NotForceLogger.log(httpRequest, NoForceReasonEnum.IP_NOT_MATCH.getCode());
//        }
        else {  // jump
            params.put(ParamKey.RESPONSE_BODY, "1");
        }

        ResponseHelper.ok(messageQueue, params, httpRequest);
    }
}
