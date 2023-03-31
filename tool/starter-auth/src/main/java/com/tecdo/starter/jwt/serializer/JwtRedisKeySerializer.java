package com.tecdo.starter.jwt.serializer;

import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * what：将 redis key 序列化为字符串
 * why：spring cache 中的简单基本类型直接使用 StringRedisSerializer 会有问题
 *
 * Created by Zeki on 2022/8/12
 **/
public class JwtRedisKeySerializer implements RedisSerializer<Object> {

	private final Charset charset;
	private final ConversionService converter;

	public JwtRedisKeySerializer() {
		this(StandardCharsets.UTF_8);
	}

	public JwtRedisKeySerializer(Charset charset) {
		Objects.requireNonNull(charset, "Charset must not be null");
		this.charset = charset;
		this.converter = DefaultConversionService.getSharedInstance();
	}

	@Override
	public Object deserialize(byte[] bytes) {
		// redis keys 会用到反序列化
		return bytes == null ? null : new String(bytes, charset);
	}

	@Override
	public byte[] serialize(Object object) {
		Objects.requireNonNull(object, "redis key is null");
		String key;
		if (object instanceof SimpleKey) {
			key = "";
		} else if (object instanceof String) {
			key = (String) object;
		} else {
			key = converter.convert(object, String.class);
		}
		return key.getBytes(this.charset);
	}

}
