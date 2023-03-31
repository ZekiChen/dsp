package com.tecdo.starter.mp.node;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 森林节点归并类
 *
 * Created by Zeki on 2022/9/14
 **/
public class ForestNodeMerger {

	/**
	 * 将节点数组归并为一个森林（多棵树）（填充节点的children域）
	 * 时间复杂度为O(n^2)
	 *
	 * @param items 节点域
	 * @return 多棵树的根节点集合
	 */
	public static <T extends INode<T>> List<T> merge(List<T> items) {
		ForestNodeManager<T> nodeManager = new ForestNodeManager<>(items);
		items.forEach(forestNode -> {
			if (forestNode.getParentId() != 0) {
				INode<T> node = nodeManager.getTreeNodeAt(forestNode.getParentId());
				if (node != null) {
					node.getChildren().add(forestNode);
				} else {
					nodeManager.addParentId(forestNode.getId());
				}
			}
		});
		return nodeManager.getRoot();
	}

	/**
	 * 根据 节点ID 检索节点域中该节点所有的子孙节点ID集
	 *
	 * @param items  节点域
	 * @param nodeId 检索的节点ID
	 * @return		 子孙节点ID集
	 */
	public static <T extends INode<T>> List<Integer> searchChildIds(List<T> items, Integer nodeId) {
		List<T> nodes = ForestNodeMerger.merge(items);
		T node = search(nodes, nodeId);
		List<Integer> res = new ArrayList<>();
		if (node != null) {
			recursiveIds(res, node);
		}
        return res;
	}

	/**
	 * 检索节点域获取指定节点信息
	 * @param nodes   节点域
	 * @param nodeId  节点ID
	 * @return        节点信息
	 */
	private static <T extends INode<T>> T search(List<T> nodes, Integer nodeId) {
		T node = null;
		for (T cur : nodes) {
			if (Objects.equals(cur.getId(), nodeId)) {
				node = cur;
				break;
			}
			if (!CollectionUtils.isEmpty(cur.getChildren())) {
				node = search(cur.getChildren(), nodeId);
				if (node != null) {
					return node;
				}
			}
		}
		return node;
	}

	/**
	 * 递归检索子孙节点ID集
	 *
	 * @param node 当前节点
	 * @return 子孙节点ID集（包含当前节点）
	 */
	private static <T extends INode<T>> List<Integer> recursiveIds(List<Integer> res, T node) {
        res.add(node.getId());
		if (!CollectionUtils.isEmpty(node.getChildren())) {
			node.getChildren().forEach(cur -> recursiveIds(res, cur));
		}
		return res;
    }

}
