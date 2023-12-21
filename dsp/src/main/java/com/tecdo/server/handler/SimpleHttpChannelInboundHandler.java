package com.tecdo.server.handler;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.common.constant.HttpCode;
import com.tecdo.constant.ParamKey;
import com.tecdo.constant.RequestPath;
import com.tecdo.controller.MessageQueue;
import com.tecdo.server.request.HttpRequest;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;


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
    String path = request.getPath();
    EventType eventType;
    Params params = Params.create();
    params.put(ParamKey.HTTP_REQUEST, request);
    switch (path) {
      case RequestPath.BID_REQUEST:
        eventType = EventType.VALIDATE_BID_REQUEST;
        break;
      case RequestPath.WIN:
        eventType = EventType.RECEIVE_WIN_NOTICE;
        break;
      case RequestPath.IMP:
        eventType = EventType.RECEIVE_IMP_NOTICE;
        break;
      case RequestPath.CLICK:
        eventType = EventType.RECEIVE_CLICK_NOTICE;
        break;
      case RequestPath.PB:
      case RequestPath.PB_AE:
        eventType = EventType.RECEIVE_PB_NOTICE;
        break;
      case RequestPath.IMP_INFO:
        eventType = EventType.RECEIVE_IMP_INFO_NOTICE;
        break;
      case RequestPath.PING:
        eventType = EventType.RECEIVE_PING_REQUEST;
        break;
      case RequestPath.LOSS:
        eventType = EventType.RECEIVE_LOSS_NOTICE;
        break;
      case RequestPath.SDK_PB:
        eventType = EventType.RECEIVE_SDK_PB_NOTICE;
        break;
      case RequestPath.FORCE:
        eventType = EventType.RECEIVE_FORCE_REQUEST;
        break;
      case RequestPath.COLLECT_FEATURE:
        eventType = EventType.RECEIVE_COLLECT_FEATURE;
        break;
      case RequestPath.COLLECT_CODE:
        eventType = EventType.RECEIVE_COLLECT_CODE;
        break;
      case RequestPath.COLLECT_ERROR:
        eventType = EventType.RECEIVE_COLLECT_ERROR;
        break;
      case RequestPath.CHECK_COUNT:
        eventType = EventType.RECEIVE_CHECK_COUNT;
          break;
      default:
        eventType = EventType.RESPONSE_RESULT;
        params.put(ParamKey.HTTP_CODE, HttpCode.NOT_FOUND);
        params.put(ParamKey.CHANNEL_CONTEXT, request.getChannelContext());
    }
    messageQueue.putMessage(eventType, params);
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

