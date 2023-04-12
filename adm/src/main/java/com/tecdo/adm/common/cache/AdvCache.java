package com.tecdo.adm.common.cache;

import cn.hutool.extra.spring.SpringUtil;
import com.tecdo.adm.api.delivery.entity.Adv;
import com.tecdo.adm.delivery.service.IAdvService;
import com.tecdo.starter.redis.CacheUtil;

import static com.tecdo.common.constant.CacheConstant.ADV_CACHE;

/**
 * Created by Zeki on 2023/4/5
 */
public class AdvCache {

	private static final String ADV_ID = "adv:id:";

	private static final IAdvService advService = SpringUtil.getBean(IAdvService.class);

	public static Adv getAdv(Integer id) {
		return CacheUtil.get(ADV_CACHE, ADV_ID, id, () -> advService.getById(id));
	}

}
