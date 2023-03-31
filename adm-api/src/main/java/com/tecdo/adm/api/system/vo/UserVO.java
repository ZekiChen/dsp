package com.tecdo.adm.api.system.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tecdo.adm.api.system.entity.User;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Zeki on 2023/3/14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("UserVO对象")
public class UserVO extends User {
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private String password;

	private String roleName;

	private String sexName;

}
