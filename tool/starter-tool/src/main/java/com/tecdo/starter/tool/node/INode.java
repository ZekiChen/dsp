package com.tecdo.starter.tool.node;

import java.io.Serializable;
import java.util.List;

/**
 * 节点接口
 *
 * Created by Zeki on 2022/9/14
 **/
public interface INode<T> extends Serializable {

	/**
	 * 主键
	 */
	Integer getId();

	/**
	 * 父主键
	 */
	Integer getParentId();

	/**
	 * 子孙节点
	 */
	List<T> getChildren();

	/**
	 * 是否有子孙节点
	 */
	default Boolean getHasChildren() {
		return false;
	}
}
