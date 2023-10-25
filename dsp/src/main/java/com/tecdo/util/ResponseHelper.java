package com.tecdo.util;

import com.tecdo.adm.api.foreign.ae.enums.AeCode;
import com.tecdo.adm.api.foreign.ae.vo.response.AeResponse;
import com.tecdo.common.constant.HttpCode;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.server.request.HttpRequest;

/**
 * Created by Zeki on 2023/4/14
 */
public class ResponseHelper {

    public static void ok(MessageQueue messageQueue, Params params, HttpRequest httpRequest) {
        params.put(ParamKey.HTTP_CODE, HttpCode.OK);
        params.put(ParamKey.CHANNEL_CONTEXT, httpRequest.getChannelContext());
        messageQueue.putMessage(EventType.RESPONSE_RESULT, params);
    }

    public static void badRequest(MessageQueue messageQueue, Params params, HttpRequest httpRequest) {
        params.put(ParamKey.HTTP_CODE, HttpCode.BAD_REQUEST);
        params.put(ParamKey.CHANNEL_CONTEXT, httpRequest.getChannelContext());
        messageQueue.putMessage(EventType.RESPONSE_RESULT, params);
    }

    public static void noBid(MessageQueue messageQueue, Params params, HttpRequest httpRequest) {
        params.put(ParamKey.HTTP_CODE, HttpCode.NOT_BID);
        params.put(ParamKey.CHANNEL_CONTEXT, httpRequest.getChannelContext());
        messageQueue.putMessage(EventType.RESPONSE_RESULT, params);
    }

    public static void aeOK(MessageQueue messageQueue, Params params, HttpRequest httpRequest) {
        params.put(ParamKey.HTTP_CODE, HttpCode.OK);
        params.put(ParamKey.RESPONSE_BODY, JsonHelper.toJSONString(new AeResponse<>(AeCode.SUCCESS)));
        params.put(ParamKey.CHANNEL_CONTEXT, httpRequest.getChannelContext());
        messageQueue.putMessage(EventType.RESPONSE_RESULT, params);
    }

    public static void aeParamError(MessageQueue messageQueue, Params params, HttpRequest httpRequest) {
        params.put(ParamKey.HTTP_CODE, HttpCode.BAD_REQUEST);
        params.put(ParamKey.RESPONSE_BODY, JsonHelper.toJSONString(new AeResponse<>(AeCode.PARAM_ERROR)));
        params.put(ParamKey.CHANNEL_CONTEXT, httpRequest.getChannelContext());
        messageQueue.putMessage(EventType.RESPONSE_RESULT, params);
    }
}
