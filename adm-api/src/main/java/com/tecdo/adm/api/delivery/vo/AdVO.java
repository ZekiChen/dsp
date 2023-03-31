package com.tecdo.adm.api.delivery.vo;

import com.tecdo.adm.api.delivery.entity.Ad;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Zeki on 2023/3/8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "AdVO对象")
public class AdVO extends Ad {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("广告组名称")
	private String adGroupName;
	@ApiModelProperty("广告活动名称")
	private String campaignName;

	@ApiModelProperty("图片URL")
	private String imageUrl;
	@ApiModelProperty("图标URL")
	private String iconUrl;
	@ApiModelProperty("视频URL")
	private String videoUrl;

	@ApiModelProperty("图片大小")
	private String imageSize;
	@ApiModelProperty("图标大小")
	private String iconSize;
}