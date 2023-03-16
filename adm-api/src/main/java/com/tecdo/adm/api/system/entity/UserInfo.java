package com.tecdo.adm.api.system.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Zeki on 2023/3/13
 */
@Data
@ApiModel(description = "用户信息")
public class UserInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "用户")
	private User user;
	@ApiModelProperty(value = "角色集合")
	private List<String> roles;
	@ApiModelProperty(value = "权限集合")
	private List<String> permissions;

}
