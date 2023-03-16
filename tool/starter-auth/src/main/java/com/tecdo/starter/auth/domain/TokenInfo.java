package com.tecdo.starter.auth.domain;

import lombok.Data;

/**
 * Created by Zeki on 2023/3/14
 */
@Data
public class TokenInfo {

	private String token;
	private int expire;  // 秒

}
