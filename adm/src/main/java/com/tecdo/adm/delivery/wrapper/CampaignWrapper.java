package com.tecdo.adm.delivery.wrapper;

import com.tecdo.adm.api.delivery.entity.Adv;
import com.tecdo.adm.api.delivery.entity.Campaign;
import com.tecdo.adm.api.delivery.entity.CampaignRtaInfo;
import com.tecdo.adm.api.delivery.vo.CampaignRtaVO;
import com.tecdo.adm.api.delivery.vo.CampaignVO;
import com.tecdo.adm.common.cache.AdvCache;
import com.tecdo.adm.common.cache.CampaignCache;
import com.tecdo.starter.mp.support.EntityWrapper;
import com.tecdo.starter.tool.util.BeanUtil;

import java.util.Objects;

/**
 * Created by Zeki on 2023/3/7
 */
public class CampaignWrapper extends EntityWrapper<Campaign, CampaignVO> {

	public static CampaignWrapper build() {
		return new CampaignWrapper();
	}

	@Override
	public CampaignVO entityVO(Campaign campaign) {
		CampaignVO vo = Objects.requireNonNull(BeanUtil.copy(campaign, CampaignVO.class));
		if (vo.getAdvId() != null) {
			Adv adv = AdvCache.getAdv(vo.getAdvId());
			if (adv != null) {
				vo.setAdvName(adv.getName());
				vo.setAdvType(adv.getType());
			}
		}
		CampaignRtaInfo campaignRta = CampaignCache.getCampaignRta(vo.getId());
		CampaignRtaVO campaignRtaVO = BeanUtil.copy(campaignRta, CampaignRtaVO.class);
		vo.setCampaignRtaVO(campaignRtaVO);
		return vo;
	}

}