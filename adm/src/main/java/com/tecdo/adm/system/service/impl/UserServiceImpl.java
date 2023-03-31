package com.tecdo.adm.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.system.entity.User;
import com.tecdo.adm.api.system.entity.UserInfo;
import com.tecdo.adm.api.system.mapper.UserMapper;
import com.tecdo.adm.api.system.vo.UserVO;
import com.tecdo.adm.common.constant.AdmConstant;
import com.tecdo.adm.system.service.IUserService;
import com.tecdo.adm.system.wrapper.UserWrapper;
import com.tecdo.starter.auth.AuthUtil;
import com.tecdo.starter.log.exception.ServiceException;
import com.tecdo.starter.mp.support.PCondition;
import com.tecdo.starter.mp.support.PQuery;
import com.tecdo.starter.tool.BigTool;
import com.tecdo.starter.tool.util.DateUtil;
import com.tecdo.starter.tool.util.DigestUtil;
import com.tecdo.starter.tool.util.StringUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Zeki on 2023/3/14
 */
@Service
@AllArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
	private static final String GUEST_NAME = "guest";

//	private final IRoleService roleService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean submit(User user) {
		if (BigTool.isNotEmpty(user.getPassword())) {
			user.setPassword(DigestUtil.encrypt(user.getPassword()));
		}
		Long userCount = baseMapper.selectCount(Wrappers.<User>query().lambda().eq(User::getAccount, user.getAccount()));
		if (userCount > 0L && BigTool.isEmpty(user.getId())) {
			throw new ServiceException(StringUtil.format("当前用户 [{}] 已存在!", user.getAccount()));
		}
		return save(user);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateUser(User user) {
		Long userCount = baseMapper.selectCount(
			Wrappers.<User>query().lambda()
				.eq(User::getAccount, user.getAccount())
				.notIn(User::getId, user.getId())
		);
		if (userCount > 0L) {
			throw new ServiceException(StringUtil.format("当前用户 [{}] 已存在!", user.getAccount()));
		}
		return updateUserInfo(user);
	}

	@Override
	public boolean updateUserInfo(User user) {
		user.setPassword(null);
		return updateById(user);
	}

	@Override
	public IPage<User> selectUserPage(IPage<User> page, User user) {
		return page.setRecords(baseMapper.selectUserPage(page, user));
	}

	@Override
	public IPage<UserVO> selectUserSearch(UserVO user, PQuery query) {
		LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>query().lambda();
		if (StringUtil.isNotBlank(user.getRealName())) {
			queryWrapper.like(User::getRealName, user.getRealName());
		}
		IPage<User> pages = this.page(PCondition.getPage(query), queryWrapper);
		return UserWrapper.build().pageVO(pages);
	}

	@Override
	public User userByAccount(String account) {
		return baseMapper.selectOne(Wrappers.<User>query().lambda().eq(User::getAccount, account));
	}

	@Override
	public UserInfo userInfo(Integer userId) {
		User user = baseMapper.selectById(userId);
		return buildUserInfo(user);
	}

	@Override
	public UserInfo userInfo(String account, String password) {
		User user = baseMapper.getUser(account, password);
		return buildUserInfo(user);
	}

	private UserInfo buildUserInfo(User user) {
		if (ObjectUtil.isEmpty(user)) {
			return null;
		}
		UserInfo userInfo = new UserInfo();
		userInfo.setUser(user);
//		if (BigTool.isNotEmpty(user)) {
//			List<String> roleNames = roleService.getRoleNames(user.getRoleId());
//			userInfo.setRoles(roleNames);
//		}
		return userInfo;
	}

	@Override
	public boolean grant(String userIds, String roleIds) {
		User user = new User();
		user.setRoleId(roleIds);
		return this.update(user, Wrappers.<User>update().lambda().in(User::getId, BigTool.toIntList(userIds)));
	}

	@Override
	public boolean resetPassword(String userIds) {
		User user = new User();
		user.setPassword(DigestUtil.encrypt(AdmConstant.DEFAULT_PASSWORD));
		user.setUpdateTime(DateUtil.now());
		return this.update(user, Wrappers.<User>update().lambda().in(User::getId, BigTool.toIntList(userIds)));
	}

	@Override
	public boolean updatePassword(Integer userId, String oldPassword, String newPassword, String newPassword1) {
		User user = getById(userId);
		if (!newPassword.equals(newPassword1)) {
			throw new ServiceException("请输入正确的确认密码!");
		}
		if (!user.getPassword().equals(DigestUtil.hex(oldPassword))) {
			throw new ServiceException("原密码不正确!");
		}
		return this.update(Wrappers.<User>update().lambda().set(User::getPassword, DigestUtil.hex(newPassword)).eq(User::getId, userId));
	}

	@Override
	public boolean removeUser(String userIds) {
		if (BigTool.contains(BigTool.toIntArray(userIds), AuthUtil.getUserId())) {
			throw new ServiceException("不能删除本账号!");
		}
		return removeBatchByIds(BigTool.toIntList(userIds));
	}

}
