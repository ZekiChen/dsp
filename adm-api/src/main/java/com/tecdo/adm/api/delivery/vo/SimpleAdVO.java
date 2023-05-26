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
@ApiModel(value = "Object")
public class SimpleAdVO extends IdEntity {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("广告名称")
	private String name;
	@ApiModelProperty(value = "广告类型", notes = "AdTypeEnum")
	private Integer type;
	@ApiModelProperty(value = "广告类型名称")
	private String typeName;
	@ApiModelProperty(value = "状态", notes = "BaseStatusEnum")
	private Integer status;
}