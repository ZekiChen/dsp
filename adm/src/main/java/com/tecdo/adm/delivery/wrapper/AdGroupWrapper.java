package com.tecdo.adm.delivery.wrapper;

import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.vo.AdGroupVO;
import com.tecdo.adm.api.delivery.vo.TargetConditionVO;
import com.tecdo.adm.common.cache.AdGroupCache;
import com.tecdo.starter.mp.support.EntityWrapper;
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
		List<TargetCondition> conditions = AdGroupCache.listCondition(vo.getId());
		List<TargetConditionVO> conditionVOs = Objects.requireNonNull(BeanUtil.copy(conditions, TargetConditionVO.class));
		vo.setConditionVOs(conditionVOs);
		return vo;
	}

}