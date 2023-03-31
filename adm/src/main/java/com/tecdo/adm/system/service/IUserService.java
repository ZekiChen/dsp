package com.tecdo.adm.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.system.entity.User;
import com.tecdo.adm.api.system.entity.UserInfo;
import com.tecdo.adm.api.system.vo.UserVO;
import com.tecdo.starter.mp.support.PQuery;

/**
 * Created by Zeki on 2023/3/14
 */
public interface IUserService extends IService<User> {

	/**
	 * 新增用户
	 */
	boolean submit(User user);

	/**
	 * 修改用户
	 */
	boolean updateUser(User user);

	/**
	 * 修改用户基本信息
	 */
	boolean updateUserInfo(User user);

	/**
	 * 自定义分页
	 */
	IPage<User> selectUserPage(IPage<User> page, User user);

	/**
	 * 自定义分页
	 */
	IPage<UserVO> selectUserSearch(UserVO user, PQuery query);

	/**
	 * 根据账号获取用户
	 */
	User userByAccount(String account);

	/**
	 * 用户信息
	 */
	UserInfo userInfo(Integer userId);

	/**
	 * 用户信息
	 */
	UserInfo userInfo(String account, String password);

	/**
	 * 给用户设置角色
	 *
	 * @param userIds
	 * @param roleIds
	 * @return
	 */
	boolean grant(String userIds, String roleIds);

	/**
	 * 初始化密码
	 */
	boolean resetPassword(String userIds);

	/**
	 * 修改密码
	 */
	boolean updatePassword(Integer userId, String oldPassword, String newPassword, String newPassword1);

	/**
	 * 删除用户
	 */
	boolean removeUser(String userIds);

}
