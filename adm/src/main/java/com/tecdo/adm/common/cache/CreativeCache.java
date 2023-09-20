package com.tecdo.adm.common.cache;

import cn.hutool.extra.spring.SpringUtil;
import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.delivery.service.ICreativeService;
import com.tecdo.starter.redis.CacheUtil;

import static com.tecdo.common.constant.CacheConstant.CREATIVE_CACHE;

/**
 * Created by Zeki on 2023/3/10
 */
public class CreativeCache {

	private static final String CREATIVE_ID = "creative:id:";
	private static final String CREATIVE_BRAND = "creative:brand:";

	private static final ICreativeService creativeService = SpringUtil.getBean(ICreativeService.class);

	public static Creative getCreative(Integer creativeId) {
		return CacheUtil.get(CREATIVE_CACHE, CREATIVE_ID, creativeId, () -> creativeService.getById(creativeId));
	}

	public static String getBrandValue(Integer brandId) {
		return CacheUtil.get(CREATIVE_CACHE, CREATIVE_BRAND, brandId, () -> creativeService.getBrandById(brandId));
	}

}
