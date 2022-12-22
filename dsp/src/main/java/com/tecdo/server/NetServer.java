package com.tecdo.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;


public class NetServer {

  private Logger logger = LoggerFactory.getLogger(NetServer.class);


  public void startup(int port, ChannelInboundHandlerAdapter handlerAdapter) {

    logger.info("net port={}", port);

    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    ChannelHandler channelInitializer = new SimpleHttpChannelInitializer(handlerAdapter);

    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class) // server socket
             .childHandler(channelInitializer)
             .option(ChannelOption.SO_BACKLOG, 6000) // determining the number of connections queued
             .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
             .childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
             .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
             .childOption(ChannelOption.SO_KEEPALIVE, false);

    try {
      bootstrap.bind(port);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("The server fail to listen on port " + port);
    }

    logger.info("Server startup, listening on {}", port);
  }

}
