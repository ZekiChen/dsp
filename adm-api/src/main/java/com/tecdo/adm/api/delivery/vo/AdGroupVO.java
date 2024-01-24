package com.tecdo.adm.api.delivery.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.MultiBidStrategy;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/3/8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("AdGroupVO对象")
public class AdGroupVO extends AdGroup {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("广告活动名称")
	private String campaignName;
	@ApiModelProperty("渠道名称集")
	private String affiliateNames;
	@ApiModelProperty("国家集")
	private String countries;
	@ApiModelProperty("定向条件集")
	List<TargetConditionVO> conditionVOs;
	@ApiModelProperty("花费是否到达预算上限标识")
	private boolean isCostFull;
	@ApiModelProperty("双阶段出价模式")
	private List<MultiBidStrategyVO> strategyVOs;

	@ApiModelProperty(value = "竞价策略", notes = "BidStrategyEnum")
	private String multiBidStrategy;
	@ApiModelProperty("操作价格")
	private String multiOptPrice;

	public List<MultiBidStrategy> listStrategies() {
		List<MultiBidStrategy> strategies = strategyVOs.stream().map(e -> {
			MultiBidStrategy strategy = new MultiBidStrategyVO();
			BeanUtils.copyProperties(e, strategy);
			return strategy;
		}).collect(Collectors.toList());
		strategies.forEach(e -> e.setAdGroupId(getId()));
		return strategies;
	}

	public List<TargetCondition> listCondition() {
		List<TargetCondition> conditions = conditionVOs.stream().map(e -> {
			TargetCondition condition = new TargetCondition();
			BeanUtils.copyProperties(e, condition);
			return condition;
		}).collect(Collectors.toList());
		conditions.forEach(e -> e.setAdGroupId(getId()));
		return conditions;
	}
}