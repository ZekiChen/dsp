package com.tecdo.adm.common.cache;

import cn.hutool.extra.spring.SpringUtil;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.delivery.service.IAdService;
import com.tecdo.starter.redis.CacheUtil;

import java.util.List;

import static com.tecdo.common.constant.CacheConstant.AD_CACHE;

/**
 * Created by Zeki on 2023/3/10
 */
public class AdCache {

	private static final String ADS_AD_GROUP_IDS = "ads:adGroupId:";

	private static final IAdService adService = SpringUtil.getBean(IAdService.class);

	public static List<Ad> listAd(Integer adGroupId) {
		return CacheUtil.get(AD_CACHE, ADS_AD_GROUP_IDS, adGroupId, () -> adService.listByAdGroupId(adGroupId));
	}
}
