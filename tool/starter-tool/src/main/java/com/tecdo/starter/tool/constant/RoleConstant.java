
package com.tecdo.starter.tool.constant;

/**
 * 系统默认角色
 *
 * Created by Zeki on 2022/9/15
 **/
public interface RoleConstant {

	String ROOT = "root";  // 超级管理员
	String HAS_ROLE_ROOT = "hasRole('" + ROOT + "')";

	String ADMIN = "admin";  // 系统管理员
	String HAS_ROLE_ADMIN = "hasAnyRole('" + ROOT + "', '" + ADMIN + "')";

}
