package com.tecdo.starter.jwt.config;

import com.tecdo.starter.jwt.JwtUtil;
import com.tecdo.starter.jwt.props.JwtProperties;
import com.tecdo.starter.jwt.serializer.JwtRedisKeySerializer;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

/**
 * Created by Zeki on 2023/3/14
 */
@AllArgsConstructor
@AutoConfiguration(after = JwtRedisConfiguration.class)
@EnableConfigurationProperties({JwtProperties.class})
public class JwtConfiguration implements SmartInitializingSingleton {

	private final JwtProperties jwtProperties;
	private final RedisConnectionFactory redisConnectionFactory;

	@Override
	public void afterSingletonsInstantiated() {
		// redisTemplate 实例化
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		JwtRedisKeySerializer redisKeySerializer = new JwtRedisKeySerializer();
		JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
		// key 序列化
		redisTemplate.setKeySerializer(redisKeySerializer);
		redisTemplate.setHashKeySerializer(redisKeySerializer);
		// value 序列化
		redisTemplate.setValueSerializer(jdkSerializationRedisSerializer);
		redisTemplate.setHashValueSerializer(jdkSerializationRedisSerializer);
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.afterPropertiesSet();
		JwtUtil.setJwtProperties(jwtProperties);
		JwtUtil.setRedisTemplate(redisTemplate);
	}
}
