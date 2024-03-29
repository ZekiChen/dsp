package com.tecdo.adm.delivery.wrapper;

import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.adm.api.delivery.vo.AdVO;
import com.tecdo.adm.common.cache.AdGroupCache;
import com.tecdo.adm.common.cache.CampaignCache;
import com.tecdo.adm.common.cache.CreativeCache;
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
		if (vo.getGroupId() != null) {
			AdGroup adGroup = AdGroupCache.getAdGroup(vo.getGroupId());
			vo.setAdGroupName(adGroup.getName());
			vo.setCampaignName(CampaignCache.getCampaign(adGroup.getCampaignId()).getName());
		}
		if (vo.getType() != null) {
			AdTypeEnum adType = AdTypeEnum.of(vo.getType());
			Creative image;
			switch (adType) {
				case BANNER:
					if (vo.getImage() != null) {
						image = CreativeCache.getCreative(vo.getImage());
						vo.setImageUrl(image.getUrl());
						vo.setImageSize("w" + image.getWidth() + "h" + image.getHeight());
					}
					break;
				case NATIVE:
					image = CreativeCache.getCreative(vo.getImage());
					Creative icon = CreativeCache.getCreative(vo.getIcon());
					Creative video = CreativeCache.getCreative(vo.getVideo());
					if (vo.getIcon() != null) {
						vo.setIconUrl(icon.getUrl());
						vo.setIconSize("w" + icon.getWidth() + "h" + icon.getHeight());
					}
					if (vo.getImage() != null) {
						vo.setImageUrl(image.getUrl());
						vo.setImageSize("w" + image.getWidth() + "h" + image.getHeight());
					}
					if (vo.getVideo() != null) {
						vo.setVideoUrl(video.getUrl());
						vo.setDuration(video.getDuration());
						vo.setVideoSize("w" + video.getWidth() + "h" + video.getHeight());
					}
					break;
				case VIDEO:
					if (vo.getVideo() != null) {
						video = CreativeCache.getCreative(vo.getVideo());
						vo.setVideoUrl(video.getUrl());
						vo.setDuration(video.getDuration());
						vo.setVideoSize("w" + video.getWidth() + "h" + video.getHeight());
					}
					if (vo.getImage() != null) {
						image = CreativeCache.getCreative(vo.getImage());
						vo.setImageUrl(image.getUrl());
						vo.setImageSize("w" + image.getWidth() + "h" + image.getHeight());
					}
					break;
				case AUDIO:
					throw new ServiceException("AD type not supported yet!");
				default:
					throw new ServiceException("Unknown ad type!");
			}
		}
		return vo;
	}

}