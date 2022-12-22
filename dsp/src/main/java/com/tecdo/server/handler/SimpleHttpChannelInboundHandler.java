package com.tecdo.server.handler;

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

  public SimpleHttpChannelInboundHandler() {
    super();
  }

  private AtomicLong atomicLong = new AtomicLong();

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {

    long requestId = atomicLong.incrementAndGet();
    HttpRequest request = new HttpRequest(requestId, ctx, msg);
    router(request);

  }

  private void router(HttpRequest request) {

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

