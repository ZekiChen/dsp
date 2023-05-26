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
public class BatchAdUpdateVO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("广告ID集")
	@NotEmpty
	private List<Integer> adIds;
	@ApiModelProperty(value = "状态", notes = "BaseStatusEnum")
	private Integer status;
}