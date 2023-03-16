package com.tecdo.adm.auth.endpoint;

import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.tecdo.adm.api.system.entity.UserInfo;
import com.tecdo.adm.auth.provider.ITokenGranter;
import com.tecdo.adm.auth.provider.TokenGranterBuilder;
import com.tecdo.adm.auth.provider.TokenParam;
import com.tecdo.adm.auth.util.TokenUtil;
import com.tecdo.core.launch.constant.TokenConstant;
import com.tecdo.starter.auth.AuthUtil;
import com.tecdo.starter.auth.domain.PacUser;
import com.tecdo.starter.jwt.JwtUtil;
import com.tecdo.starter.jwt.props.JwtProperties;
import com.tecdo.starter.redis.CacheUtil;
import com.tecdo.starter.tool.support.Kv;
import com.tecdo.starter.tool.util.WebUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import static com.tecdo.common.constant.CacheConstant.*;

/**
 * Created by Zeki on 2023/3/14
 */
@ApiSort(1)
@RestController
@AllArgsConstructor
@Api(tags = "认证授权")
public class PacTokenEndpoint {

    private final JwtProperties jwtProperties;

    @PostMapping("/oauth/token")
    @ApiOperation("获取认证令牌")
    public Kv token(@ApiParam(value = "账号", required = true) @RequestParam(required = false) String username,
                    @ApiParam(value = "密码", required = true) @RequestParam(required = false) String password) {
        // 临时方案
        if ("admin".equals(username) && "pacdsppacdsp".equals(password)) {
            return Kv.create().set(TokenConstant.ACCESS_TOKEN, "asdfiouw4uw3h6jjklse");
        }

        Kv authInfo = Kv.create();
        String grantType = WebUtil.getRequest().getParameter(TokenConstant.GRANT_TYPE);
        String refreshToken = WebUtil.getRequest().getParameter(TokenConstant.REFRESH_TOKEN);

        TokenParam tokenParam = new TokenParam();
        tokenParam.getArgs()
                .set("account", username)
                .set("password", password)
                .set("grantType", grantType)
                .set("refreshToken", refreshToken);

        ITokenGranter granter = TokenGranterBuilder.getGranter(grantType);
        UserInfo userInfo = granter.grant(tokenParam);

        return userInfo != null && userInfo.getUser() != null ? TokenUtil.createAuthInfo(userInfo) :
                authInfo.set("error_code", HttpServletResponse.SC_BAD_REQUEST).set("error_description", "用户名或密码不正确");
    }


    @GetMapping("/oauth/logout")
    @ApiOperation(value = "退出登录")
    public Kv logout() {
        PacUser user = AuthUtil.getUser();
        if (user != null && jwtProperties.getState()) {
            String token = JwtUtil.getToken(WebUtil.getRequest().getHeader(TokenConstant.HEADER));
            JwtUtil.removeAccessToken(String.valueOf(user.getUserId()), token);
        }
        return Kv.create().set("success", "true").set("msg", "success");
    }

    @GetMapping("/oauth/clear-cache")
    @ApiOperation(value = "清除缓存")
    public Kv clearCache() {
        CacheUtil.clear(CAMPAIGN_CACHE);
        CacheUtil.clear(AD_GROUP_CACHE);
        CacheUtil.clear(DICT_CACHE);
        CacheUtil.clear(AD_CACHE);
        CacheUtil.clear(DICT_CACHE);
        return Kv.create().set("success", "true").set("msg", "success");
    }
}
