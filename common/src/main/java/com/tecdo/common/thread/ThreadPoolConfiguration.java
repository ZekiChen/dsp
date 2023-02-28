package com.tecdo.common.thread;

import com.tecdo.common.cache.config.PacRedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Zeki on 2023/2/28
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PacRedisProperties.class)
public class ThreadPoolConfiguration {
}
