package com.tecdo.adm.auth.util;

import com.tecdo.adm.api.system.entity.User;
import com.tecdo.adm.api.system.entity.UserInfo;
import com.tecdo.core.launch.constant.TokenConstant;
import com.tecdo.starter.auth.domain.TokenInfo;
import com.tecdo.starter.secure.SecureUtil;
import com.tecdo.starter.tool.BigTool;
import com.tecdo.starter.tool.support.Kv;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zeki on 2023/3/13
 */
public class TokenUtil {

	public final static String TOKEN_NOT_PERMISSION = "令牌授权已过期";

	/**
	 * 创建token
	 */
	public static Kv createAuthInfo(UserInfo userInfo) {
		User user = userInfo.getUser();
		Map<String, Object> jwtParam = new HashMap<>(16);
		jwtParam.put(TokenConstant.TOKEN_TYPE, TokenConstant.ACCESS_TOKEN);
		jwtParam.put(TokenConstant.USER_ID, BigTool.toStr(user.getId()));
		jwtParam.put(TokenConstant.ACCOUNT, user.getAccount());
		jwtParam.put(TokenConstant.REAL_NAME, user.getRealName());
		jwtParam.put(TokenConstant.ROLE_ID, user.getRoleId());
		jwtParam.put(TokenConstant.ROLE_NAME, BigTool.join(userInfo.getRoles()));

		Kv authInfo = Kv.create();
		try {
			TokenInfo accessToken = SecureUtil.createJWT(jwtParam, "audience", "issuser", TokenConstant.ACCESS_TOKEN);
			return authInfo.set(TokenConstant.USER_ID, BigTool.toStr(user.getId()))
				.set(TokenConstant.ACCOUNT, user.getAccount())
				.set(TokenConstant.REAL_NAME, user.getRealName())
				.set(TokenConstant.ROLE_ID, user.getRoleId())
				.set(TokenConstant.ROLE_NAME, BigTool.join(userInfo.getRoles()))
				.set(TokenConstant.ACCESS_TOKEN, accessToken.getToken())
				.set(TokenConstant.REFRESH_TOKEN, createRefreshToken(userInfo).getToken())
				.set(TokenConstant.TOKEN_TYPE, TokenConstant.BEARER)
				.set(TokenConstant.EXPIRES_IN, accessToken.getExpire());
		} catch (Exception ex) {
			return authInfo.set("error_code", HttpServletResponse.SC_UNAUTHORIZED).set("error_description", ex.getMessage());
		}
	}

	private static TokenInfo createRefreshToken(UserInfo userInfo) {
		User user = userInfo.getUser();
		Map<String, Object> jwtParam = new HashMap<>(16);
		jwtParam.put(TokenConstant.TOKEN_TYPE, TokenConstant.REFRESH_TOKEN);
		jwtParam.put(TokenConstant.USER_ID, BigTool.toStr(user.getId()));
		jwtParam.put(TokenConstant.ROLE_ID, BigTool.toStr(user.getRoleId()));
		return SecureUtil.createJWT(jwtParam, "audience", "issuser", TokenConstant.REFRESH_TOKEN);
	}

}
