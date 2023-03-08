package com.tecdo.adm.delivery.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.vo.AdVO;
import com.tecdo.starter.mp.support.EntityWrapper;

import java.util.Objects;

/**
 * 包装类，返回视图层所需的字段
 *
 * Created by Zeki on 2023/3/7
 */
public class AdWrapper extends EntityWrapper<Ad, AdVO> {

	public static AdWrapper build() {
		return new AdWrapper();
	}

	@Override
	public AdVO entityVO(Ad ad) {
		AdVO adVO = Objects.requireNonNull(BeanUtil.copyProperties(ad, AdVO.class));
		return adVO;
	}

}