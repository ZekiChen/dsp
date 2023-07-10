package com.tecdo.adm.api.delivery.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Zeki on 2023/3/8
 */
@Data
@ApiModel(value = "Object")
public class BatchAdGroupUpdateVO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("广告组ID集")
	@NotEmpty
	private List<Integer> adGroupIds;
	@ApiModelProperty("操作价格")
	private Double optPrice;
	@ApiModelProperty(value = "竞价策略", notes = "BidStrategyEnum")
	private Integer bidStrategy;
	@ApiModelProperty("日预算")
	private Double dailyBudget;
	@ApiModelProperty("底价乘数,只在bidStrategy=4时生效")
	private Double bidMultiplier;
	@ApiModelProperty(value = "状态", notes = "BaseStatusEnum")
	private Integer status;
}