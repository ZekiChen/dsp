package com.tecdo.starter.mp.node;

import lombok.Data;

import java.util.Objects;

/**
 * 树型节点类
 *
 * Created by Zeki on 2022/9/14
 **/
@Data
public class TreeNode extends BaseNode<TreeNode> {

	private static final long serialVersionUID = 1L;

	private String title;
	private Integer key;
	private Integer value;

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		TreeNode other = (TreeNode) obj;
		return Objects.equals(this.getId(), other.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, parentId);
	}

}
