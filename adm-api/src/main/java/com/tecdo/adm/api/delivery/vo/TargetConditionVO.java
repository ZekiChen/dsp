package com.tecdo.adm.api.delivery.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Zeki on 2023/3/8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "TargetConditionVO对象")
public class TargetConditionVO extends TargetCondition {

	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private Integer adGroupId;
}