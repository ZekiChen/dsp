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
@ApiModel(value = "SimpleAdGroupUpdateVO对象")
public class SimpleAdGroupUpdateVO extends IdEntity {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("广告组名")
	private String name;
	@ApiModelProperty("操作价格")
	private Double optPrice;
	@ApiModelProperty(value = "竞价策略", notes = "BidStrategyEnum")
	private Integer bidStrategy;
	@ApiModelProperty("日预算")
	private Double dailyBudget;
	@ApiModelProperty("备注")
	private String remark;
	@ApiModelProperty("状态")
	private Integer status;
}