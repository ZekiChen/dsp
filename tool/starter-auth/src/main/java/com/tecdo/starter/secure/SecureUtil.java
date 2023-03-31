package com.tecdo.starter.secure;

import com.tecdo.core.launch.constant.TokenConstant;
import com.tecdo.starter.auth.AuthUtil;
import com.tecdo.starter.auth.domain.TokenInfo;
import com.tecdo.starter.auth.exception.SecureException;
import com.tecdo.starter.jwt.JwtUtil;
import com.tecdo.starter.jwt.props.JwtProperties;
import com.tecdo.starter.secure.provider.IClientDetails;
import com.tecdo.starter.secure.provider.IClientDetailsService;
import com.tecdo.starter.tool.BigTool;
import com.tecdo.starter.tool.util.*;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.SneakyThrows;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.*;

import static com.tecdo.starter.secure.constant.SecureConstant.*;

/**
 * Created by Zeki on 2023/3/14
 */
public class SecureUtil extends AuthUtil {
	private final static String CLIENT_ID = TokenConstant.CLIENT_ID;

	private static IClientDetailsService clientDetailsService;

	private static JwtProperties jwtProperties;

	private static IClientDetailsService getClientDetailsService() {
		clientDetailsService = clientDetailsService == null ? SpringUtil.getBean(IClientDetailsService.class) : clientDetailsService;
		return clientDetailsService;
	}

	private static JwtProperties getJwtProperties() {
		jwtProperties = jwtProperties == null ? SpringUtil.getBean(JwtProperties.class) : jwtProperties;
		return jwtProperties;
	}

	/**
	 * 创建令牌
	 */
	public static TokenInfo createJWT(Map<String, Object> user, String audience, String issuer, String tokenType) {

		String[] tokens = extractAndDecodeHeader();
		String clientId = tokens[0];
		String clientSecret = tokens[1];

		IClientDetails clientDetails = clientDetails(clientId);

		if (!validateClient(clientDetails, clientId, clientSecret)) {
			throw new SecureException("客户端认证失败, 请检查请求头 [Authorization] 信息");
		}

		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);

		// 生成签名密钥
		byte[] apiKeySecretBytes = Base64.getDecoder().decode(JwtUtil.getBase64Security());
		Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

		// 添加构成JWT的类
		JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT")
			.setIssuer(issuer)
			.setAudience(audience)
			.signWith(signingKey);

		// 设置JWT参数
		user.forEach(builder::claim);

		// 设置应用id
		builder.claim(CLIENT_ID, clientId);

		//添加Token过期时间
		long expireMillis;
		if (tokenType.equals(TokenConstant.ACCESS_TOKEN)) {
			expireMillis = clientDetails.getAccessTokenValidity() * 1000L;
		} else if (tokenType.equals(TokenConstant.REFRESH_TOKEN)) {
			expireMillis = clientDetails.getRefreshTokenValidity() * 1000L;
		} else {
			expireMillis = getExpire();
		}
		long expMillis = nowMillis + expireMillis;
		Date exp = new Date(expMillis);
		builder.setExpiration(exp).setNotBefore(now);

		// 组装Token信息
		TokenInfo tokenInfo = new TokenInfo();
		tokenInfo.setToken(builder.compact());
		tokenInfo.setExpire((int) (expireMillis / 1000L));

		// Token状态配置, 仅在生成 AccessToken 时候执行
		if (getJwtProperties().getState() && TokenConstant.ACCESS_TOKEN.equals(tokenType)) {
			String userId = String.valueOf(user.get(TokenConstant.USER_ID));
			JwtUtil.addAccessToken(userId, tokenInfo.getToken(), tokenInfo.getExpire());
		}
		// Token状态配置, 仅在生成 RefreshToken 时候执行
		if (getJwtProperties().getState() && getJwtProperties().getSingle() && TokenConstant.REFRESH_TOKEN.equals(tokenType)) {
			String userId = String.valueOf(user.get(TokenConstant.USER_ID));
			JwtUtil.addRefreshToken(userId, tokenInfo.getToken(), tokenInfo.getExpire());
		}
		return tokenInfo;
	}

	/**
	 * 获取过期时间(次日凌晨3点)
	 *
	 * @return expire
	 */
	public static long getExpire() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, 1);
		cal.set(Calendar.HOUR_OF_DAY, 3);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis() - System.currentTimeMillis();
	}

	/**
	 * 客户端信息解码
	 */
	@SneakyThrows
	public static String[] extractAndDecodeHeader() {
		// 获取请求头客户端信息
		String header = Objects.requireNonNull(WebUtil.getRequest()).getHeader(BASIC_HEADER_KEY);
		header = BigTool.toStr(header).replace(BASIC_HEADER_PREFIX_EXT, BASIC_HEADER_PREFIX);
		if (!header.startsWith(BASIC_HEADER_PREFIX)) {
			throw new SecureException("未获取到请求头[Authorization]的信息");
		}
		byte[] base64Token = header.substring(6).getBytes(Charsets.UTF_8_NAME);

		byte[] decoded;
		try {
			decoded = Base64.getDecoder().decode(base64Token);
		} catch (IllegalArgumentException var7) {
			throw new RuntimeException("客户端令牌解析失败");
		}

		String token = new String(decoded, Charsets.UTF_8_NAME);
		int index = token.indexOf(StringPool.COLON);
		if (index == -1) {
			throw new RuntimeException("客户端令牌不合法");
		} else {
			return new String[]{token.substring(0, index), token.substring(index + 1)};
		}
	}

	/**
	 * 获取客户端信息
	 */
	private static IClientDetails clientDetails(String clientId) {
		return getClientDetailsService().loadClientByClientId(clientId);
	}

	/**
	 * 校验Client
	 */
	private static boolean validateClient(IClientDetails clientDetails, String clientId, String clientSecret) {
		return clientDetails != null
				&& StringUtil.equals(clientId, clientDetails.getClientId())
				&& StringUtil.equals(clientSecret, clientDetails.getClientSecret());
	}

}
