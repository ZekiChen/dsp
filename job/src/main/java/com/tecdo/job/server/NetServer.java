package com.tecdo.job.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetServer {

    public void startup(int port, ChannelInboundHandlerAdapter handlerAdapter) {

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

        log.info("Netty server startup, listening on: {}", port);
    }

}
