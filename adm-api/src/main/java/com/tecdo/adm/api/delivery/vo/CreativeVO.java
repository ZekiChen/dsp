package com.tecdo.adm.api.delivery.vo;

import com.tecdo.adm.api.delivery.entity.Creative;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Zeki on 2023/3/8
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "CreativeVO对象")
public class CreativeVO extends Creative {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("品牌的名称")
	private String brandName;
}