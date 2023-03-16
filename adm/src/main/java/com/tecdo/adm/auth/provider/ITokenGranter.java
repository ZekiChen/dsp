package com.tecdo.adm.auth.provider;

import com.tecdo.adm.api.system.entity.UserInfo;

/**
 * Created by Zeki on 2023/3/13
 */
public interface ITokenGranter {

	UserInfo grant(TokenParam tokenParam);

}
