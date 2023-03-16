package com.tecdo.starter.jwt.constant;

/**
 * Created by Zeki on 2023/3/14
 */
public interface JwtConstant {

	/**
	 * 默认key
	 */
	String DEFAULT_SECRET_KEY = "wearepacdevelopmentwebelievetecdobusinesstecdoleadtheworldtoyou";

	/**
	 * key安全长度，具体见：https://tools.ietf.org/html/rfc7518#section-3.2
	 */
	int SECRET_KEY_LENGTH = 32;

}
