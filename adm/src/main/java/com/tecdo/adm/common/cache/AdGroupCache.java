package com.tecdo.adm.common.cache;

import cn.hutool.extra.spring.SpringUtil;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.delivery.service.IAdGroupService;
import com.tecdo.adm.delivery.service.ITargetConditionService;
import com.tecdo.starter.redis.CacheUtil;

import java.util.Collections;
import java.util.List;

import static com.tecdo.common.constant.CacheConstant.AD_GROUP_CACHE;

/**
 * Created by Zeki on 2023/3/9
 */
public class AdGroupCache {

	private static final String CONDITIONS_AD_GROUP_ID = "conditions:adGroupId:";
	private static final String AD_GROUP_ID = "adGroup:id:";
	private static final String AD_GROUP_CAMPAIGN_ID = "adGroup:campaignId:";

	private static final IAdGroupService adGroupService = SpringUtil.getBean(IAdGroupService.class);
	private static final ITargetConditionService conditionService = SpringUtil.getBean(ITargetConditionService.class);

	public static List<TargetCondition> listCondition(Integer adGroupId) {
		return CacheUtil.get(AD_GROUP_CACHE, CONDITIONS_AD_GROUP_ID, adGroupId, () -> conditionService.listCondition(adGroupId));
	}

	public static AdGroup getAdGroup(Integer id) {
		return CacheUtil.get(AD_GROUP_CACHE, AD_GROUP_ID, id, () -> adGroupService.getById(id));
	}

	public static List<AdGroup> listAdGroup(Integer campaignId) {
		return CacheUtil.get(AD_GROUP_CACHE, AD_GROUP_CAMPAIGN_ID, campaignId, () ->
				adGroupService.listByCampaignIds(Collections.singletonList(campaignId)));
	}
}
