package com.tecdo.adm.auth.grant;

import com.tecdo.adm.api.system.entity.UserInfo;
import com.tecdo.adm.auth.provider.ITokenGranter;
import com.tecdo.adm.auth.provider.TokenParam;
import com.tecdo.adm.system.service.IUserService;
import com.tecdo.starter.tool.BigTool;
import com.tecdo.starter.tool.util.DigestUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Created by Zeki on 2023/3/13
 */
@Component
@AllArgsConstructor
public class PasswordGranter implements ITokenGranter {

    public static final String GRANT_TYPE = "password";

    private final IUserService userService;

    @Override
    public UserInfo grant(TokenParam tokenParam) {
        String account = tokenParam.getArgs().getStr("account");
        String password = tokenParam.getArgs().getStr("password");
        return (BigTool.isAnyBlank(account, password) ? null :
                userService.userInfo(account, DigestUtil.hex(password)));
    }

}
