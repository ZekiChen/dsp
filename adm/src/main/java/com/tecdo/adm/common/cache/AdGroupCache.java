package com.tecdo.adm.common.cache;

import cn.hutool.extra.spring.SpringUtil;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.delivery.service.ITargetConditionService;
import com.tecdo.starter.redis.CacheUtil;

import java.util.List;

import static com.tecdo.common.constant.CacheConstant.AD_GROUP_CACHE;

/**
 * Created by Zeki on 2023/3/9
 */
public class AdGroupCache {

	private static final String CONDITIONS_AD_GROUP_ID = "conditions:adGroupId:";

	private static final ITargetConditionService conditionService = SpringUtil.getBean(ITargetConditionService.class);

	public static List<TargetCondition> listCondition(Integer adGroupId) {
		return CacheUtil.get(AD_GROUP_CACHE, CONDITIONS_AD_GROUP_ID, adGroupId, () -> conditionService.listCondition(adGroupId));
	}

}
