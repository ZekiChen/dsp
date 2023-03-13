package com.tecdo.starter.mp.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.tecdo.starter.tool.BigTool;
import com.tecdo.starter.tool.support.Kv;
import com.tecdo.starter.tool.util.BeanUtil;
import com.tecdo.starter.tool.util.StringUtil;

import java.util.Map;

/**
 * 分页工具
 *
 * Created by Zeki on 2022/8/26
 **/
public class PCondition {

	@SuppressWarnings("rawtypes")
	public static final IPage EMPTY_PAGE = new Page<>();

	/**
	 * 转化成 MP 中的 Page
	 *
	 * @param query 查询条件
	 * @return IPage
	 */
	public static <T> IPage<T> getPage(PQuery query) {
		Page<T> page = new Page<>(BigTool.toInt(query.getCurrent(), 1), BigTool.toInt(query.getSize(), 10));
		setPageQuery(query, page);
		return page;
	}

	/**
	 * 转化成 MP 中的 PageDTO
	 * @param query 查询条件
	 * @return PageDTO
	 */
	public static <T> PageDTO<T> getPageDTO(PQuery query) {
		PageDTO<T> page =  new PageDTO<>(BigTool.toInt(query.getCurrent(), 1), BigTool.toInt(query.getSize(), 10));
		setPageQuery(query, page);
		return page;
	}

	private static void setPageQuery(PQuery query, Page page) {
		String[] ascArr = BigTool.toStrArray(query.getAscs());
		for (String asc : ascArr) {
			page.addOrder(OrderItem.asc(StringUtil.cleanIdentifier(asc)));
		}
		String[] descArr = BigTool.toStrArray(query.getDescs());
		for (String desc : descArr) {
			page.addOrder(OrderItem.desc(StringUtil.cleanIdentifier(desc)));
		}
		if (Boolean.FALSE.equals(query.getSearchCount())) {
			page.setTotal(-1);
		}
	}

	/**
	 * 获取 MP 中的 QueryWrapper
	 *
	 * @param entity 实体
	 * @param <T>    类型
	 * @return QueryWrapper
	 */
	public static <T> QueryWrapper<T> getQueryWrapper(T entity) {
		return new QueryWrapper<>(entity);
	}

	/**
	 * 获取 MP 中的 QueryWrapper（自动剔除条件查询）
	 *
	 * @param query 查询条件
	 * @param clazz 实体类
	 * @param <T>   类型
	 * @return QueryWrapper
	 */
	public static <T> QueryWrapper<T> getQueryWrapper(Map<String, Object> query, Class<T> clazz) {
		Kv exclude = Kv.create()
				.set("current", "current")
				.set("size", "size")
				.set("ascs", "ascs")
				.set("descs", "descs");
		return getQueryWrapper(query, exclude, clazz);
	}

	/**
	 * 获取 MP 中的 QueryWrapper
	 *
	 * @param query   查询条件
	 * @param exclude 排除的查询条件
	 * @param clazz   实体类
	 * @param <T>     类型
	 * @return QueryWrapper
	 */
	private static <T> QueryWrapper<T> getQueryWrapper(Map<String, Object> query, Map<String, Object> exclude, Class<T> clazz) {
		exclude.forEach((k, v) -> query.remove(k));
		QueryWrapper<T> qw = new QueryWrapper<>();
		qw.setEntity(BeanUtil.instantiateClass(clazz));
		SqlKeyword.buildCondition(query, qw);
		return qw;
	}

	@SuppressWarnings("unchecked")
	public static <T> IPage<T> emptyPage() {
		return (IPage<T>) EMPTY_PAGE;
	}

}