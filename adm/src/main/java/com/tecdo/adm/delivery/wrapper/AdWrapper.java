package com.tecdo.adm.delivery.wrapper;

import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.adm.api.delivery.vo.AdVO;
import com.tecdo.adm.common.cache.AdCache;
import com.tecdo.starter.log.exception.ServiceException;
import com.tecdo.starter.mp.support.EntityWrapper;
import com.tecdo.starter.tool.util.BeanUtil;

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
		AdVO vo = Objects.requireNonNull(BeanUtil.copy(ad, AdVO.class));
		AdTypeEnum adType = AdTypeEnum.of(vo.getType());
		switch (adType) {
			case BANNER:
				vo.setImageUrl(AdCache.getCreative(vo.getImage()).getUrl());
				break;
			case NATIVE:
				vo.setImageUrl(AdCache.getCreative(vo.getImage()).getUrl());
				vo.setIconUrl(AdCache.getCreative(vo.getIcon()).getUrl());
				break;
			case VIDEO:
				vo.setVideoUrl(AdCache.getCreative(vo.getVideo()).getUrl());
				break;
			case AUDIO:
				throw new ServiceException("AD type not supported yet!");
			default:
				throw new ServiceException("Unknown ad type!");
		}
		return vo;
	}

}