package com.tecdo.adm.api.delivery.vo;

import com.tecdo.starter.mp.entity.IdEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeki on 2023/3/8
 */
@Data
@ApiModel(value = "SimpleAdGroupUpdateVO对象")
public class SimpleAdGroupUpdateVO extends IdEntity {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("操作价格")
	private Double optPrice;
	@ApiModelProperty("日预算")
	private Double dailyBudget;
}