package com.tecdo.adm.auth.provider;

import cn.hutool.extra.spring.SpringUtil;
import com.tecdo.adm.auth.grant.PasswordGranter;
import com.tecdo.adm.auth.grant.RefreshTokenGranter;
import com.tecdo.starter.auth.exception.SecureException;
import com.tecdo.starter.tool.BigTool;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Zeki on 2023/3/13
 */
@AllArgsConstructor
public class TokenGranterBuilder {

	private static final Map<String, ITokenGranter> GRANTER_POOL = new ConcurrentHashMap<>();

	static {
		GRANTER_POOL.put(PasswordGranter.GRANT_TYPE, SpringUtil.getBean(PasswordGranter.class));
		GRANTER_POOL.put(RefreshTokenGranter.GRANT_TYPE, SpringUtil.getBean(RefreshTokenGranter.class));
	}

	public static ITokenGranter getGranter(String grantType) {
		grantType = BigTool.toStr(grantType, PasswordGranter.GRANT_TYPE);
		ITokenGranter tokenGranter = GRANTER_POOL.get(grantType);
		if (tokenGranter == null) {
			throw new SecureException("no grantType was found!");
		} else {
			return GRANTER_POOL.get(grantType);
		}
	}

}
