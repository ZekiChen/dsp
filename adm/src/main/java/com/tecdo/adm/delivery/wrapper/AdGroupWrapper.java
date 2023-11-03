package com.tecdo.adm.delivery.wrapper;

import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.MultiBidStrategy;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.adm.api.delivery.vo.AdGroupVO;
import com.tecdo.adm.api.delivery.vo.MultiBidStrategyVO;
import com.tecdo.adm.api.delivery.vo.TargetConditionVO;
import com.tecdo.adm.common.cache.AdGroupCache;
import com.tecdo.adm.common.cache.AffiliateCache;
import com.tecdo.adm.common.cache.CampaignCache;
import com.tecdo.starter.mp.support.EntityWrapper;
import com.tecdo.starter.tool.BigTool;
import com.tecdo.starter.tool.util.BeanUtil;

import java.util.List;
import java.util.Objects;

/**
 * Created by Zeki on 2023/3/7
 */
public class AdGroupWrapper extends EntityWrapper<AdGroup, AdGroupVO> {

	public static AdGroupWrapper build() {
		return new AdGroupWrapper();
	}

	@Override
	public AdGroupVO entityVO(AdGroup adGroup) {
		AdGroupVO vo = Objects.requireNonNull(BeanUtil.copy(adGroup, AdGroupVO.class));
		vo.setCampaignName(CampaignCache.getCampaign(vo.getCampaignId()).getName());

		List<TargetCondition> conditions = AdGroupCache.listCondition(vo.getId());
		List<TargetConditionVO> conditionVOs = Objects.requireNonNull(BeanUtil.copy(conditions, TargetConditionVO.class));


		List<MultiBidStrategy> strategies = AdGroupCache.listStrategy(vo.getId());
		List<MultiBidStrategyVO> strategyVOs = Objects.requireNonNull(BeanUtil.copy(strategies, MultiBidStrategyVO.class));

		vo.setConditionVOs(conditionVOs);
		vo.setStrategyVOs(strategyVOs);
		setAffNames(vo, conditionVOs);
		setCountries(vo, conditionVOs);

		return vo;
	}

	private static void setAffNames(AdGroupVO vo, List<TargetConditionVO> conditionVOs) {
		TargetConditionVO affCondition = conditionVOs.stream()
				.filter(e -> ConditionEnum.AFFILIATE.getDesc().equals(e.getAttribute()))
				.findFirst().orElse(null);
		if (affCondition != null) {
			String affiliateIds = affCondition.getValue();
			List<String> affNames = AffiliateCache.listName(BigTool.toIntList(affiliateIds));
			vo.setAffiliateNames(BigTool.join(affNames));
		}
	}

	private void setCountries(AdGroupVO vo, List<TargetConditionVO> conditionVOs) {
		conditionVOs.stream()
				.filter(e -> ConditionEnum.DEVICE_COUNTRY.getDesc().equals(e.getAttribute()))
				.findFirst().ifPresent(affCondition -> vo.setCountries(affCondition.getValue()));
	}

}