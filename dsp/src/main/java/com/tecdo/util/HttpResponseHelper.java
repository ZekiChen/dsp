package com.tecdo.util;

import com.tecdo.common.util.Params;
import com.tecdo.constant.ParamKey;

import java.util.Map;
import java.util.Objects;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class HttpResponseHelper {

  public static void reply(Params params) {
    DefaultFullHttpResponse response;
    Boolean responseImage = params.get(ParamKey.RESPONSE_IMAGE, false);
    if (responseImage) {
      response =
        buildImageResponse(params.get(ParamKey.HTTP_CODE), params.get(ParamKey.RESPONSE_BODY));
    } else {
      response = HttpResponseHelper.buildResponse(params.get(ParamKey.HTTP_CODE),
                                                  params.get(ParamKey.RESPONSE_BODY));
    }

    addHeaders(response, params.get(ParamKey.RESPONSE_HEADER));

    ChannelHandlerContext context = params.get(ParamKey.CHANNEL_CONTEXT);

    context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
  }

  private static void addHeaders(DefaultFullHttpResponse response, Map<String, Object> headerMap) {
    if (headerMap != null) {
      headerMap.entrySet()
               .stream()
               .filter(entry -> Objects.nonNull(entry.getValue()))
               // do not overwrite content length
               .filter(entry -> !HttpHeaderNames.CONTENT_LENGTH.toString()
                                                               .equalsIgnoreCase(entry.getKey()))
               .forEach(entry -> response.headers().add(entry.getKey(), entry.getValue()));
    }
  }

  /**
   * Build response with code
   */
  private static DefaultFullHttpResponse buildResponse(int code, String responseBody) {
    byte[] bytes = null == responseBody ? new byte[0] : responseBody.getBytes();

    DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                   HttpResponseStatus.valueOf(code),
                                                                   Unpooled.wrappedBuffer(bytes));
    response.headers().add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());


    return response;
  }

  private static DefaultFullHttpResponse buildImageResponse(int code, byte[] bytes) {

    DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                   HttpResponseStatus.valueOf(code),
                                                                   Unpooled.wrappedBuffer(bytes));
    response.headers()
            .add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes())
            .add(HttpHeaderNames.CONTENT_TYPE, "image/png");

    return response;
  }

}
