package com.tecdo.config;

import com.tecdo.server.NetServer;
import com.tecdo.server.handler.SimpleHttpChannelInboundHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Zeki on 2022/12/28
 **/
@Configuration
public class NettyConfig {

    @Value("${server.port}")
    private int serverPort;

    @Bean
    public NetServer netServer() {
        NetServer server = new NetServer();
        server.startup(serverPort, new SimpleHttpChannelInboundHandler());
        return server;
    }
}
