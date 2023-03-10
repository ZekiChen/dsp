package com.tecdo.starter.mp;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.tecdo.starter.mp.enums.BaseStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MP 字段自动填充功能
 *
 * Created by Zeki on 2022/8/24
 **/
@Slf4j
@Component
public class PacMetaObjectHandler implements MetaObjectHandler {

	@Override
	public void insertFill(MetaObject metaObject) {
		Date now = new Date();
		this.strictInsertFill(metaObject, "status", Integer.class, BaseStatusEnum.ACTIVE.getType());
		this.strictInsertFill(metaObject, "createTime", Date.class, now);
		this.strictInsertFill(metaObject, "updateTime", Date.class, now);
	}

	@Override
	public void updateFill(MetaObject metaObject) {
		this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
	}

}