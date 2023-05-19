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
public class BundleAdGroupUpdateVO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("广告组ID集")
	@NotEmpty
	private List<Integer> adGroupIds;
	@ApiModelProperty("比较符")
	private String operation;
	@ApiModelProperty("条件值")
	private String value;
}