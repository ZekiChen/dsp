package com.tecdo.core.launch.thread.config;

import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.core.launch.thread.props.ThreadProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Created by Zeki on 2023/2/28
 */
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(ThreadProperties.class)
public class ThreadConfiguration {

    private final ThreadProperties properties;

    @Bean
    public ThreadPool threadPool() {
        return new ThreadPool(properties.getCoreSize());
    }
}
