package com.tecdo.adm.api.delivery.vo;

import com.tecdo.adm.api.delivery.entity.Campaign;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Zeki on 2023/3/8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "CampaignVO对象", description = "CampaignVO对象")
public class CampaignVO extends Campaign {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("Campaign RTA信息")
	private CampaignRtaVO campaignRtaVO;

}