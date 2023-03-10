package com.tecdo.adm.api.system.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tecdo.adm.api.system.entity.Dict;
import com.tecdo.starter.tool.node.INode;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("DictVO对象")
public class DictVO extends Dict implements INode<DictVO> {

	private static final long serialVersionUID = 1L;

	/**
	 * 子孙节点
	 */
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<DictVO> children;

	@Override
	public List<DictVO> getChildren() {
		if (this.children == null) {
			this.children = new ArrayList<>();
		}
		return this.children;
	}

	/**
	 * 上级字典
	 */
	private String parentName;
}
