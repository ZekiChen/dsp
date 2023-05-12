package com.tecdo.adm.common.cache;

import cn.hutool.extra.spring.SpringUtil;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.delivery.service.IAffiliateService;
import com.tecdo.starter.redis.CacheUtil;

import java.util.List;
import java.util.stream.Collectors;

import static com.tecdo.common.constant.CacheConstant.AFF_CACHE;

/**
 * Created by Zeki on 2023/3/10
 */
public class AffiliateCache {

	private static final String AFF_ID = "affiliate:id:";

	private static final IAffiliateService service = SpringUtil.getBean(IAffiliateService.class);

	public static Affiliate getById(Integer id) {
		return CacheUtil.get(AFF_CACHE, AFF_ID, id, () -> service.getById(id));
	}

	public static List<String> listName(List<Integer> ids) {
		return ids.stream().map(AffiliateCache::getById).map(Affiliate::getName).collect(Collectors.toList());
	}
}
