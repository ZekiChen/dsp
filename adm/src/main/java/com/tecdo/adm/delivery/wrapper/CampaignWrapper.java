package com.tecdo.adm.delivery.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tecdo.adm.api.delivery.entity.Campaign;
import com.tecdo.adm.api.delivery.vo.CampaignVO;
import com.tecdo.starter.mp.support.EntityWrapper;

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
		CampaignVO campaignVO = Objects.requireNonNull(BeanUtil.copyProperties(campaign, CampaignVO.class));
		return campaignVO;
	}

}