package com.tecdo.starter.redis.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @author sisyphus.su
 * @description:
 * @date: 2023-04-27 11:09
 **/
@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private String port;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.af.database:3}")
    private int db;

    @Bean
    public Config redissionConfig() {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress(String.format("redis://%s:%s", host, port));
        singleServerConfig.setDatabase(db);
        if (StringUtils.hasText(password)) {
            singleServerConfig.setPassword(password);
        }

        return config;
    }

    @Bean
    public RedissonClient redissonClient() {
        return Redisson.create(redissionConfig());
    }
}


