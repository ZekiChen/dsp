package com.tecdo.adm.delivery.wrapper;

import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.vo.AffiliateVO;
import com.tecdo.starter.mp.support.EntityWrapper;
import com.tecdo.starter.tool.util.BeanUtil;

import java.util.Objects;

/**
 * Created by Zeki on 2023/3/15
 */
public class AffiliateWrapper extends EntityWrapper<Affiliate, AffiliateVO> {

	public static AffiliateWrapper build() {
		return new AffiliateWrapper();
	}

	@Override
	public AffiliateVO entityVO(Affiliate affiliate) {
		AffiliateVO vo = Objects.requireNonNull(BeanUtil.copy(affiliate, AffiliateVO.class));
		return vo;
	}

}