package com.tecdo.adm.common.cache;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tecdo.adm.api.delivery.entity.Campaign;
import com.tecdo.adm.api.delivery.entity.CampaignRtaInfo;
import com.tecdo.adm.delivery.service.ICampaignRtaService;
import com.tecdo.adm.delivery.service.ICampaignService;
import com.tecdo.starter.redis.CacheUtil;
import com.tecdo.starter.tool.BigTool;

import java.util.List;

import static com.tecdo.common.constant.CacheConstant.CAMPAIGN_CACHE;

/**
 * Created by Zeki on 2023/3/9
 */
public class CampaignCache {

	private static final String CAMPAIGN_ID = "campaign:id:";
	private static final String CAMPAIGNS_IDS = "campaigns:ids:";
	private static final String CAMPAIGN_RTA_ID = "campaignRta:campaignId:";
	private static final String CAMPAIGN_RTAS_IDS = "campaignRtas:campaignIds:";

	private static final ICampaignService campaignService = SpringUtil.getBean(ICampaignService.class);
	private static final ICampaignRtaService campaignRtaService = SpringUtil.getBean(ICampaignRtaService.class);

	public static Campaign getCampaign(Integer id) {
		return CacheUtil.get(CAMPAIGN_CACHE, CAMPAIGN_ID, id, () -> campaignService.getById(id));
	}

	public static List<Campaign> listCampaign(String ids) {
		return CacheUtil.get(CAMPAIGN_CACHE, CAMPAIGNS_IDS, ids, () -> campaignService.listByIds(BigTool.toIntList(ids)));
	}

	public static CampaignRtaInfo getCampaignRta(Integer campaignId) {
		return CacheUtil.get(CAMPAIGN_CACHE, CAMPAIGN_RTA_ID, campaignId, () -> campaignRtaService.getByCampaignId(campaignId));
	}

	public static List<CampaignRtaInfo> listCampaignRta(String channel, String advCampaignIds) {
		return CacheUtil.get(CAMPAIGN_CACHE, CAMPAIGN_RTAS_IDS,
				channel.concat(StrUtil.COLON).concat(advCampaignIds), () ->
						campaignRtaService.list(Wrappers.<CampaignRtaInfo>lambdaQuery()
								.eq(CampaignRtaInfo::getChannel, channel)
								.in(CampaignRtaInfo::getAdvCampaignId, BigTool.toStrList(advCampaignIds))));
	}
}
