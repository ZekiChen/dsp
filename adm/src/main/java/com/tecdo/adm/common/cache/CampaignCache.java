package com.tecdo.adm.common.cache;

import cn.hutool.extra.spring.SpringUtil;
import com.tecdo.adm.api.delivery.entity.CampaignRtaInfo;
import com.tecdo.adm.delivery.service.ICampaignRtaService;
import com.tecdo.adm.delivery.service.ICampaignService;
import com.tecdo.starter.redis.CacheUtil;

import static com.tecdo.common.constant.CacheConstant.CAMPAIGN_CACHE;

/**
 * Created by Zeki on 2023/3/9
 */
public class CampaignCache {

	private static final String CAMPAIGN_ID = "campaign:id:";
	private static final String CAMPAIGN_RTA_ID = "campaignRta:campaignId:";

	private static final ICampaignRtaService campaignRtaService = SpringUtil.getBean(ICampaignRtaService.class);
	private static final ICampaignService campaignService = SpringUtil.getBean(ICampaignService.class);

	public static CampaignRtaInfo getCampaignRta(Integer campaignId) {
		return CacheUtil.get(CAMPAIGN_CACHE, CAMPAIGN_RTA_ID, campaignId, () -> campaignRtaService.getByCampaignId(campaignId));
	}

}
