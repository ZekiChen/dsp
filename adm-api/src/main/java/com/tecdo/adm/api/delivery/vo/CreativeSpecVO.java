package com.tecdo.adm.api.delivery.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by Zeki on 2023/3/8
 */
@Data
@ApiModel(value = "CreativeSpecVO对象")
public class CreativeSpecVO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("素材宽度")
	private Integer width;
	@ApiModelProperty("素材高度")
	private Integer height;
}