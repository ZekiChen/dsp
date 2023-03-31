package com.tecdo.starter.jwt.props;

import com.tecdo.starter.jwt.constant.JwtConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by Zeki on 2023/3/14
 */
@Data
@ConfigurationProperties("pac.token")
public class JwtProperties {

	/**
	 * token是否有状态
	 */
	private Boolean state = Boolean.FALSE;

	/**
	 * 是否只可同时在线一人
	 */
	private Boolean single = Boolean.FALSE;

	/**
	 * token签名
	 */
	private String signKey = JwtConstant.DEFAULT_SECRET_KEY;

	/**
	 * 获取签名规则
	 */
	public String getSignKey() {
		if (this.signKey.length() < JwtConstant.SECRET_KEY_LENGTH) {
			return JwtConstant.DEFAULT_SECRET_KEY;
		}
		return this.signKey;
	}

}
