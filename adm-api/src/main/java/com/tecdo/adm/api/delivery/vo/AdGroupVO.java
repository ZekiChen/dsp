package com.tecdo.adm.api.delivery.vo;

import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.starter.tool.util.BeanUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Objects;

/**
 * Created by Zeki on 2023/3/8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("AdGroupVO对象")
public class AdGroupVO extends AdGroup {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("定向条件集")
	List<TargetConditionVO> conditionVOs;

	public List<TargetCondition> listCondition() {
		List<TargetCondition> conditions = Objects.requireNonNull(BeanUtil.copy(conditionVOs, TargetCondition.class));
		conditions.forEach(e -> e.setAdGroupId(getId()));
		return conditions;
	}
}