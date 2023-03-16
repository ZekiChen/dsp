package com.tecdo.adm.system.wrapper;

import com.tecdo.adm.api.system.entity.User;
import com.tecdo.adm.api.system.vo.UserVO;
import com.tecdo.starter.mp.support.EntityWrapper;
import com.tecdo.starter.tool.util.BeanUtil;

import java.util.Objects;

public class UserWrapper extends EntityWrapper<User, UserVO> {

	public static UserWrapper build() {
		return new UserWrapper();
 	}

	@Override
	public UserVO entityVO(User user) {
		UserVO userVO = Objects.requireNonNull(BeanUtil.copy(user, UserVO.class));
//		List<String> roleNames = SysCache.getRoleNames(userVO.getRoleId());
//		userVO.setRoleName(BigTool.join(roleNames));
//		List<String> roleNameZhs = SysCache.getRoleNameZhs(userVO.getRoleId());
//		userVO.setRoleNameZh(BigTool.join(roleNameZhs));
		return userVO;
	}
}