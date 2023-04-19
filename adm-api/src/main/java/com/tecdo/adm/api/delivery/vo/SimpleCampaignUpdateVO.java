package com.tecdo.adm.api.delivery.vo;

import com.tecdo.starter.mp.entity.IdEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Zeki on 2023/3/8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "SimpleCampaignUpdateVO对象")
public class SimpleCampaignUpdateVO extends IdEntity {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("日预算")
	private Double dailyBudget;
	@ApiModelProperty("状态")
	private Integer status;
}