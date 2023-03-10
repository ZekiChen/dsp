package com.tecdo.starter.redis.config;

import com.tecdo.starter.redis.CacheUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

/**
 * Created by Zeki on 2023/3/9
 */
@Order
@AutoConfiguration(after = RedisAutoConfiguration.class)
public class CacheConfiguration {

	@Bean("redisCacheManager")
	@ConditionalOnMissingBean(name = "redisCacheManager")
	public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
		RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(Duration.ofHours(1));
		return RedisCacheManager
			.builder(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
			.cacheDefaults(redisCacheConfiguration).build();
	}

	@Bean
	public CacheUtil cacheUtil() {
		return new CacheUtil();
	}

}
