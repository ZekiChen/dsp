package com.tecdo.starter.mp.support;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 分页查询条件参数 工具
 *
 * Created by Zeki on 2022/8/26
 **/
@Data
@Accessors(chain = true)  // chain=true：生成 setter() 返回 this，代替了默认的返回 void
@ApiModel(description = "查询条件")
public class PQuery {

	@ApiModelProperty(value = "当前页")
	private Integer current;
	@ApiModelProperty(value = "每页显示条数")
	private Integer size;
	@ApiModelProperty(hidden = true, notes = "正排序规则，支持传入多个字段，逗号分割")
	private String ascs;
	@ApiModelProperty(hidden = true, notes = "倒排序规则，支持传入多个字段，逗号分割")
	private String descs;
	@ApiModelProperty(hidden = true, notes = "是否进行count查询, 默认 true")
	private Boolean searchCount;
}