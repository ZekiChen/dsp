package com.tecdo.common.thread.config;

import com.tecdo.common.thread.ThreadPool;
import com.tecdo.common.thread.props.ThreadProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Zeki on 2023/2/28
 */
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@EnableConfigurationProperties(ThreadProperties.class)
public class ThreadConfiguration {

    private final ThreadProperties properties;

    @Bean
    public ThreadPool threadPool() {
        return new ThreadPool(properties.getCoreSize());
    }
}
