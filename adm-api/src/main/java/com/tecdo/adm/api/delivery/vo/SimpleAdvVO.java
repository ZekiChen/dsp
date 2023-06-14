package com.tecdo.adm.api.delivery.vo;

import com.tecdo.starter.mp.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Zeki on 2023/3/8
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "VO对象")
public class SimpleAdvVO extends BaseVO {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "类型", notes = "AdvTypeEnum")
	private Integer type;
}