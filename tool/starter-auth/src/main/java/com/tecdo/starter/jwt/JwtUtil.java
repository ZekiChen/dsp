package com.tecdo.starter.jwt;

import com.tecdo.starter.jwt.props.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ObjectUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * JWT 工具类
 *
 * Created by Zeki on 2022/8/12
 **/
public class JwtUtil {

	/**
	 * token基础配置
	 */
	public static String BEARER = "bearer";
	public static Integer AUTH_LENGTH = 7;

	/**
	 * token保存至redis的key
	 */
	private static final String REFRESH_TOKEN_CACHE = "pac:refreshToken";
	private static final String TOKEN_CACHE = "pac:token";
	private static final String TOKEN_KEY = "token:state:";

	private static JwtProperties jwtProperties;

	private static RedisTemplate<String, Object> redisTemplate;

	public static JwtProperties getJwtProperties() {
		return jwtProperties;
	}

	public static void setJwtProperties(JwtProperties properties) {
		if (JwtUtil.jwtProperties == null) {
			JwtUtil.jwtProperties = properties;
		}
	}

	public static RedisTemplate<String, Object> getRedisTemplate() {
		return redisTemplate;
	}

	public static void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
		if (JwtUtil.redisTemplate == null) {
			JwtUtil.redisTemplate = redisTemplate;
		}
	}

	/**
	 * 签名加密
	 */
	public static String getBase64Security() {
		return Base64.getEncoder().encodeToString(getJwtProperties().getSignKey().getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 获取请求传递的token串
	 *
	 * @param auth token
	 * @return String
	 */
	public static String getToken(String auth) {
		if ((auth != null) && (auth.length() > AUTH_LENGTH)) {
			String headStr = auth.substring(0, 6).toLowerCase();
			if (headStr.compareTo(BEARER) == 0) {
				auth = auth.substring(7);
			}
			return auth;
		}
		return null;
	}

	/**
	 * 解析JWT
	 *
	 * @param jwt token串
	 * @return Claims
	 */
	public static Claims parseJWT(String jwt) {
		try {
			return Jwts.parserBuilder()
				.setSigningKey(Base64.getDecoder().decode(getBase64Security())).build()
				.parseClaimsJws(jwt).getBody();
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * 获取保存在redis的accessToken
	 *
	 * @param userId      用户id
	 * @param accessToken accessToken
	 * @return accessToken
	 */
	public static String getAccessToken(String userId, String accessToken) {
		return String.valueOf(getRedisTemplate().opsForValue().get(getAccessTokenKey(userId, accessToken)));
	}


	/**
	 * 添加accessToken至redis
	 *
	 * @param userId      用户id
	 * @param accessToken accessToken
	 * @param expire      过期时间
	 */
	public static void addAccessToken(String userId, String accessToken, int expire) {
		getRedisTemplate().delete(getAccessTokenKey(userId, accessToken));
		getRedisTemplate().opsForValue().set(getAccessTokenKey(userId, accessToken), accessToken, expire, TimeUnit.SECONDS);
	}

	/**
	 * 删除保存在redis的accessToken
	 *
	 * @param userId   用户id
	 */
	public static void removeAccessToken(String userId) {
		removeAccessToken(userId, null);
	}

	/**
	 * 删除保存在redis的accessToken
	 *
	 * @param userId      用户id
	 * @param accessToken accessToken
	 */
	public static void removeAccessToken(String userId, String accessToken) {
		getRedisTemplate().delete(getAccessTokenKey(userId, accessToken));
	}

	/**
	 * 获取accessToken索引
	 *
	 * @param userId      用户id
	 * @param accessToken accessToken
	 * @return token索引
	 */
	public static String getAccessTokenKey(String userId, String accessToken) {
		String key = TOKEN_CACHE.concat("::").concat(TOKEN_KEY);
		if (getJwtProperties().getSingle() || ObjectUtils.isEmpty(accessToken)) {
			return key.concat(userId);
		} else {
			return key.concat(accessToken);
		}
	}

	/**
	 * 获取保存在redis的refreshToken
	 *
	 * @param userId       用户id
	 * @param refreshToken refreshToken
	 * @return accessToken
	 */
	public static String getRefreshToken(String userId, String refreshToken) {
		return String.valueOf(getRedisTemplate().opsForValue().get(getRefreshTokenKey(userId)));
	}

	/**
	 * 添加refreshToken至redis
	 *
	 * @param userId       用户id
	 * @param refreshToken refreshToken
	 * @param expire       过期时间
	 */
	public static void addRefreshToken(String userId, String refreshToken, int expire) {
		getRedisTemplate().delete(getRefreshTokenKey(userId));
		getRedisTemplate().opsForValue().set(getRefreshTokenKey(userId), refreshToken, expire, TimeUnit.SECONDS);
	}

	/**
	 * 删除保存在refreshToken的token
	 *
	 * @param userId   用户id
	 */
	public static void removeRefreshToken(String userId) {
		getRedisTemplate().delete(getRefreshTokenKey(userId));
	}

	/**
	 * 获取refreshToken索引
	 *
	 * @param userId   用户id
	 * @return token索引
	 */
	public static String getRefreshTokenKey(String userId) {
		return REFRESH_TOKEN_CACHE.concat("::").concat(TOKEN_KEY).concat(userId);
	}

}
