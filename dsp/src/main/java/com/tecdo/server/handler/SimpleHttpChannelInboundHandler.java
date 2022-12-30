package com.tecdo.server.handler;

import com.tecdo.common.Params;
import com.tecdo.constant.Constant;
import com.tecdo.constant.EventType;
import com.tecdo.constant.HttpCode;
import com.tecdo.constant.ParamKey;
import com.tecdo.constant.RequestPath;
import com.tecdo.controller.MessageQueue;
import com.tecdo.server.request.HttpRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;


@Sharable
public class SimpleHttpChannelInboundHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  private static Logger logger = LoggerFactory.getLogger(SimpleHttpChannelInboundHandler.class);

  public SimpleHttpChannelInboundHandler(MessageQueue messageQueue) {
    super();
    this.messageQueue = messageQueue;
  }

  private final AtomicLong atomicLong = new AtomicLong();

  private final MessageQueue messageQueue;

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {

    long requestId = atomicLong.incrementAndGet();
    HttpRequest request = new HttpRequest(requestId, ctx, msg);
    router(request);

  }

  private void router(HttpRequest request) {
    String path = getPath(request.getUri());
    EventType eventType;
    Params params = Params.create();
    switch (path) {
      case RequestPath.BID_REQUEST:
        eventType = EventType.VALIDATE_BID_REQUEST;
        params.put(ParamKey.HTTP_REQUEST, request);
        break;
      default:
        eventType = EventType.RESPONSE_RESULT;
        params.put(ParamKey.HTTP_CODE, HttpCode.NOT_FOUND);
        params.put(ParamKey.CHANNEL_CONTEXT, request.getChannelContext());
    }
    messageQueue.putMessage(eventType, params);
  }

  private String getPath(String uri) {
    int index = uri.indexOf(Constant.QUESTION_MARK);
    if (0 < index) {
      uri = uri.substring(0, index);
    }
    return uri;
  }


  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    try {
      super.channelReadComplete(ctx);
      ctx.channel().flush();
    } catch (Exception e) {
      logger.error("Failed in channel read complete !", e);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    logger.info(String.format("An exceptionCaught() event was fired,%s", cause));
    if (null != cause) {
      cause.printStackTrace();
    }
    if (null != ctx) {
      ctx.channel().close();
    }
  }

}

