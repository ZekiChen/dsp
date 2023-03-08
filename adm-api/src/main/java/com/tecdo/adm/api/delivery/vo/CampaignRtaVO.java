package com.tecdo.adm.api.delivery.vo;

import com.tecdo.adm.api.delivery.entity.CampaignRtaInfo;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Zeki on 2023/3/8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "CampaignRtaVO对象", description = "CampaignRtaVO对象")
public class CampaignRtaVO extends CampaignRtaInfo {

	private static final long serialVersionUID = 1L;

}