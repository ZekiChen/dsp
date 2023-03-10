package com.tecdo.adm.delivery.wrapper;

import com.tecdo.starter.tool.util.BeanUtil;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.vo.AdGroupVO;
import com.tecdo.starter.mp.support.EntityWrapper;

import java.util.Objects;

/**
 * 包装类，返回视图层所需的字段
 *
 * Created by Zeki on 2023/3/7
 */
public class AdGroupWrapper extends EntityWrapper<AdGroup, AdGroupVO> {

	public static AdGroupWrapper build() {
		return new AdGroupWrapper();
	}

	@Override
	public AdGroupVO entityVO(AdGroup adGroup) {
		AdGroupVO adGroupVO = Objects.requireNonNull(BeanUtil.copy(adGroup, AdGroupVO.class));
		return adGroupVO;
	}

}