package com.tecdo.server;

import com.tecdo.server.handler.CustomExceptionHandler;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

public class SimpleHttpChannelInitializer extends ChannelInitializer<SocketChannel> {


  private ChannelInboundHandlerAdapter channelInboundHandlerAdapter;

  public SimpleHttpChannelInitializer(ChannelInboundHandlerAdapter handlerAdapter) {
    this.channelInboundHandlerAdapter = handlerAdapter;
  }

  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin()
                                             .allowCredentials()
                                             .allowNullOrigin()
                                             .allowedRequestHeaders(HttpHeaderNames.CONTENT_TYPE)
                                             .allowedRequestMethods(HttpMethod.GET, HttpMethod.POST)
                                             .build();
    ChannelPipeline pipe = ch.pipeline();
    pipe.addLast("readTimeoutHandler", new ReadTimeoutHandler(60))
            .addLast("writeTimeoutHandler", new WriteTimeoutHandler(60))
            .addLast("customExceptionHandler", new CustomExceptionHandler())
            .addLast("decoder", new HttpRequestDecoder())
            .addLast("encoder", new HttpResponseEncoder())
            .addLast("compressor", new HttpContentCompressor())
            .addLast("deCompressor", new HttpContentDecompressor())
            .addLast("aggregator", new HttpObjectAggregator(65536))
            .addLast("streamer", new ChunkedWriteHandler())
            .addLast(new CorsHandler(corsConfig))
            .addLast("handler", channelInboundHandlerAdapter);
  }

}

