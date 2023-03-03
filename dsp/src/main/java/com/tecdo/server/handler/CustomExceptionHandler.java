package com.tecdo.server.handler;

import com.tecdo.common.constant.HttpCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;

public class CustomExceptionHandler extends ChannelDuplexHandler {

  private static final Logger log = LoggerFactory.getLogger(CustomExceptionHandler.class);

  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    if (cause instanceof ReadTimeoutException || cause instanceof WriteTimeoutException) {
      log.error("Custom handle {} catch an exception", ctx, cause);

      DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                     HttpResponseStatus.valueOf(
                                                                       HttpCode.BAD_REQUEST),
                                                                     Unpooled.wrappedBuffer(
                                                                       "Timeout".getBytes()));

      ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    } else {
      super.exceptionCaught(ctx, cause);
    }
  }

}
