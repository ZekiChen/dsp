package com.tecdo.adm.api.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.starter.mp.entity.BaseEntity;
import com.tecdo.starter.mp.enums.BaseStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户表
 *
 * Created by Zeki on 2023/3/13
 */
@Data
@TableName("user")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * 账号
	 */
	private String account;
	/**
	 * 密码
	 */
	private String password;
	/**
	 * 姓名
	 */
	private String realName;
	/**
	 * 邮箱
	 */
	private String email;
	/**
	 * 手机
	 */
	private String phone;
	/**
	 * 性别
	 */
	private Integer sex;
	/**
	 * 角色id
	 */
	private String roleId;
	/**
	 * 状态
	 * @see BaseStatusEnum
	 */
	@TableField(fill = FieldFill.INSERT)
	private Integer status;

}
