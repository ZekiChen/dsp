package com.tecdo.adm.api.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tecdo.adm.api.system.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Zeki on 2023/3/14
 */
public interface UserMapper extends BaseMapper<User> {

	/**
	 * 自定义分页
	 */
	List<User> selectUserPage(IPage<User> page, @Param("user") User user);

	/**
	 * 获取用户
	 */
	User getUser(String account, String password);

}
