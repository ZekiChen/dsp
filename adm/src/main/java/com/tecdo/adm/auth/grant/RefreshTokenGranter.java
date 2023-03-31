package com.tecdo.adm.auth.grant;

import com.tecdo.adm.api.system.entity.UserInfo;
import com.tecdo.adm.auth.provider.ITokenGranter;
import com.tecdo.adm.auth.provider.TokenParam;
import com.tecdo.adm.auth.util.TokenUtil;
import com.tecdo.adm.system.service.IUserService;
import com.tecdo.core.launch.constant.TokenConstant;
import com.tecdo.starter.jwt.JwtUtil;
import com.tecdo.starter.jwt.props.JwtProperties;
import com.tecdo.starter.log.exception.ServiceException;
import com.tecdo.starter.tool.BigTool;
import com.tecdo.starter.tool.util.StringUtil;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Created by Zeki on 2023/3/14
 */
@Component
@AllArgsConstructor
public class RefreshTokenGranter implements ITokenGranter {

	public static final String GRANT_TYPE = "refresh_token";

	private final IUserService userService;
//	private final IRoleService roleService;
	private final JwtProperties jwtProperties;

	@Override
	public UserInfo grant(TokenParam tokenParam) {
		String grantType = tokenParam.getArgs().getStr("grantType");
		String refreshToken = tokenParam.getArgs().getStr("refreshToken");
		UserInfo userInfo = null;
		if (BigTool.isNoneBlank(grantType, refreshToken) && grantType.equals(TokenConstant.REFRESH_TOKEN)) {
			if (!judgeRefreshToken(refreshToken)) {
				throw new ServiceException(TokenUtil.TOKEN_NOT_PERMISSION);
			}
			Claims claims = JwtUtil.parseJWT(refreshToken);
			if (claims != null) {
				String tokenType = BigTool.toStr(claims.get(TokenConstant.TOKEN_TYPE));
				if (tokenType.equals(TokenConstant.REFRESH_TOKEN)) {
					userInfo = userService.userInfo(BigTool.toInt(claims.get(TokenConstant.USER_ID)));
				}
			}
		}
		return userInfo;
	}

	private boolean judgeRefreshToken(String refreshToken) {
		if (jwtProperties.getState() && jwtProperties.getSingle()) {
			Claims claims = JwtUtil.parseJWT(refreshToken);
			String userId = String.valueOf(claims.get("user_id"));
			String token = JwtUtil.getRefreshToken(userId, refreshToken);
			return StringUtil.equalsIgnoreCase(token, refreshToken);
		}
		return true;
	}

}
