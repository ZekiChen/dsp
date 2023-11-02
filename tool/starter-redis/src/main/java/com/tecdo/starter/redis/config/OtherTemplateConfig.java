package com.tecdo.starter.redis.config;

import com.tecdo.starter.redis.serializer.RedisKeySerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * 其他区域的 RedisTemplate  配置
 *
 * Created by Zeki on 2023/11/1
 */
@AutoConfiguration(before = RedisAutoConfiguration.class)
public class OtherTemplateConfig {

    @Value("${pac.redis-mx.database}")
    private int mxDB;
    @Value("${pac.redis-mx.host}")
    private String mxHost;
    @Value("${pac.redis-mx.port:6379}")
    private int mxPort;
    @Value("${pac.redis-mx.password}")
    private String mxPassword;

    @Value("${pac.redis-par.database}")
    private int parDB;
    @Value("${pac.redis-par.host}")
    private String parHost;
    @Value("${pac.redis-par.port:6379}")
    private int parPort;
    @Value("${pac.redis-par.password}")
    private String parPassword;

    @Bean(name = "mxRedisTemplate")
    @ConditionalOnProperty("pac.redis-mx.host")
    @ConditionalOnMissingBean(name = "mxRedisTemplate")
    public RedisTemplate<String, Object> mxRedisTemplate(RedisSerializer<Object> redisSerializer) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // key 序列化
        RedisKeySerializer keySerializer = new RedisKeySerializer();
        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setHashKeySerializer(keySerializer);
        // value 序列化
        redisTemplate.setValueSerializer(redisSerializer);
        redisTemplate.setHashValueSerializer(redisSerializer);

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(mxHost);
        config.setPort(mxPort);
        config.setPassword(RedisPassword.of(mxPassword));
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(config);
        connectionFactory.setDatabase(mxDB);
        connectionFactory.afterPropertiesSet();

        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }

    @Bean(name = "parRedisTemplate")
    @ConditionalOnProperty("pac.redis-par.host")
    @ConditionalOnMissingBean(name = "parRedisTemplate")
    public RedisTemplate<String, Object> parRedisTemplate(RedisSerializer<Object> redisSerializer) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // key 序列化
        RedisKeySerializer keySerializer = new RedisKeySerializer();
        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setHashKeySerializer(keySerializer);
        // value 序列化
        redisTemplate.setValueSerializer(redisSerializer);
        redisTemplate.setHashValueSerializer(redisSerializer);

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(parHost);
        config.setPort(parPort);
        config.setPassword(RedisPassword.of(parPassword));
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(config);
        connectionFactory.setDatabase(parDB);
        connectionFactory.afterPropertiesSet();

        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }
}
