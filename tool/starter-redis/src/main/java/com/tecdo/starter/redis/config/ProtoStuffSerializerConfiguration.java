package com.tecdo.starter.redis.config;

import com.tecdo.starter.redis.serializer.ProtoStuffSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * ProtoStuff 序列化配置
 *
 * Created by Zeki on 2023/5/4
 */
@AutoConfiguration(before = RedisTemplateConfiguration.class)
@ConditionalOnClass(name = "io.protostuff.Schema")
public class ProtoStuffSerializerConfiguration implements PacRedisSerializerConfigAble {

	@Bean
	@ConditionalOnMissingBean
	@Override
	public RedisSerializer<Object> redisSerializer(PacRedisProperties properties) {
		if (PacRedisProperties.SerializerType.ProtoStuff == properties.getSerializerType()) {
			return new ProtoStuffSerializer();
		}
		return defaultRedisSerializer(properties);
	}

}