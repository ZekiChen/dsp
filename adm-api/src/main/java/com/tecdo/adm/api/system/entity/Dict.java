package com.tecdo.adm.api.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tecdo.starter.mp.entity.IdEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典表
 *
 * Created by Zeki on 2022/9/15
 **/
@Data
@TableName("dict")
@EqualsAndHashCode(callSuper = true)
@ApiModel("Dict对象")
public class Dict extends IdEntity {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "父主键")
	private Integer parentId;
	@ApiModelProperty(value = "字典码")
	private String code;
	@ApiModelProperty(value = "字典值")
	private String dictKey;
	@ApiModelProperty(value = "字典名称")
	private String dictValue;
	@ApiModelProperty(value = "排序")
	private Integer sort;
	@ApiModelProperty(value = "字典备注")
	private String remark;
	@ApiModelProperty(value = "是否已封存")
	private Integer isSealed;
}