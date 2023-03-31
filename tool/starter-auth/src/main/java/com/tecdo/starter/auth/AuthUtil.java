package com.tecdo.starter.auth;

import com.tecdo.starter.auth.domain.PacUser;
import com.tecdo.starter.auth.exception.SecureException;
import com.tecdo.starter.jwt.JwtUtil;
import com.tecdo.starter.jwt.props.JwtProperties;
import com.tecdo.starter.tool.BigTool;
import com.tecdo.starter.tool.constant.RoleConstant;
import com.tecdo.starter.tool.support.Kv;
import com.tecdo.starter.tool.util.SpringUtil;
import com.tecdo.starter.tool.util.StringPool;
import com.tecdo.starter.tool.util.StringUtil;
import com.tecdo.starter.tool.util.WebUtil;
import io.jsonwebtoken.Claims;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

import static com.tecdo.core.launch.constant.TokenConstant.*;

/**
 * Auth 工具类
 *
 * Created by Zeki on 2022/8/18
 **/
public class AuthUtil {

	private static final String PAC_USER_REQUEST_ATTR = "_PAC_USER_REQUEST_ATTR_";

	private static JwtProperties jwtProperties = SpringUtil.getBean(JwtProperties.class);

	public static PacUser getUser() {
		return getUser(true);
	}

	public static PacUser getUser(boolean verify) {
		HttpServletRequest request = WebUtil.getRequest();
		if (request == null) {
			if (verify) {
				throw new SecureException("用户信息为空！");
			}
			return null;
		}
		// 优先从 request 中获取
		Object pacUser = request.getAttribute(PAC_USER_REQUEST_ATTR);
		if (pacUser == null) {
			pacUser = getUser(request);
			if (pacUser == null && verify) {
				throw new SecureException("用户信息为空！");
			}
			if (pacUser != null) {
				// 设置到 request 中
				request.setAttribute(PAC_USER_REQUEST_ATTR, pacUser);
			}
		}
		return (PacUser) pacUser;
	}

	@SuppressWarnings("unchecked")
	public static PacUser getUser(HttpServletRequest request) {
		Claims claims = getClaims(request);
		if (claims == null) {
			return null;
		}
		String clientId = BigTool.toStr(claims.get(CLIENT_ID));
		Integer userId = BigTool.toInt(claims.get(USER_ID));
		String account = BigTool.toStr(claims.get(ACCOUNT));
		String roleId = BigTool.toStr(claims.get(ROLE_ID));
		String roleName = BigTool.toStr(claims.get(ROLE_NAME));
		Kv detail = Kv.create().setAll((Map<? extends String, ?>) claims.get(DETAIL));
		PacUser pacUser = new PacUser();
		pacUser.setClientId(clientId);
		pacUser.setUserId(userId);
		pacUser.setAccount(account);
		pacUser.setRoleId(roleId);
		pacUser.setRoleName(roleName);
		pacUser.setDetail(detail);
		return pacUser;
	}

	public static boolean isRoot() {
		return StringUtil.containsAny(getRoleName(), RoleConstant.ROOT);
	}

	public static boolean isAdmin() {
		return isAdmin(getRoleName());
	}

	public static boolean isAdmin(String roleName) {
		return StringUtil.containsAny(roleName, RoleConstant.ADMIN, RoleConstant.ROOT);
	}

	public static Integer getUserId() {
		PacUser user = getUser();
		return (null == user) ? -1 : user.getUserId();
	}

	public static Integer getUserId(HttpServletRequest request) {
		PacUser user = getUser(request);
		return (null == user) ? -1 : user.getUserId();
	}

	public static String getAccount() {
		PacUser user = getUser();
		return (null == user) ? StringPool.EMPTY : user.getAccount();
	}

	public static String getAccount(HttpServletRequest request) {
		PacUser user = getUser(request);
		return (null == user) ? StringPool.EMPTY : user.getAccount();
	}

	public static String getRealName() {
		PacUser user = getUser();
		return (null == user) ? StringPool.EMPTY : user.getRealName();
	}

	public static String getRealName(HttpServletRequest request) {
		PacUser user = getUser(request);
		return (null == user) ? StringPool.EMPTY : user.getRealName();
	}

	public static String getRoleName() {
		PacUser user = getUser();
		return (null == user) ? StringPool.EMPTY : user.getRoleName();
	}

	public static String getRoleName(HttpServletRequest request) {
		PacUser user = getUser(request);
		return (null == user) ? StringPool.EMPTY : user.getRoleName();
	}

	public static String getClientId() {
		PacUser user = getUser();
		return (null == user) ? StringPool.EMPTY : user.getClientId();
	}

	public static String getClientId(HttpServletRequest request) {
		PacUser user = getUser(request);
		return (null == user) ? StringPool.EMPTY : user.getClientId();
	}

	public static Kv getDetail() {
		PacUser user = getUser();
		return (null == user) ? Kv.create() : user.getDetail();
	}

	public static Kv getDetail(HttpServletRequest request) {
		PacUser user = getUser(request);
		return (null == user) ? Kv.create() : user.getDetail();
	}

	public static Claims getClaims(HttpServletRequest request) {
		String auth = request.getHeader(HEADER);
		Claims claims = null;
		String token;
		// 获取 Token 参数
		if (StringUtil.isNotBlank(auth)) {
			token = JwtUtil.getToken(auth);
		} else {
			String parameter = request.getParameter(HEADER);
			token = JwtUtil.getToken(parameter);
		}
		// 获取 Token 值
		if (StringUtil.isNotBlank(token)) {
			claims = JwtUtil.parseJWT(token);
		}
		// 判断 Token 状态
		if (!ObjectUtils.isEmpty(claims) && jwtProperties.getState()) {
			String userId = BigTool.toStr(claims.get(USER_ID));
			String accessToken = JwtUtil.getAccessToken(userId, token);
			if (!token.equalsIgnoreCase(accessToken)) {
				return null;
			}
		}
		return claims;
	}

	public static String getHeader() {
		return getHeader(Objects.requireNonNull(WebUtil.getRequest()));
	}

	public static String getHeader(HttpServletRequest request) {
		return request.getHeader(HEADER);
	}

}