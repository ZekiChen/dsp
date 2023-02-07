package com.tecdo.common.cache.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * redis 属性配置
 *
 * Created by Zeki on 2023/2/6
 **/
@Getter
@Setter
@ConfigurationProperties("pac.redis")
public class PacRedisProperties {

	/**
	 * 序列化方式
	 */
	private SerializerType serializerType = SerializerType.ProtoStuff;

	public enum SerializerType {
		/**
		 * 默认：ProtoStuff 序列化
		 */
		ProtoStuff,
		/**
		 * json 序列化
		 */
		JSON,
		/**
		 * jdk 序列化
		 */
		JDK
	}
}
