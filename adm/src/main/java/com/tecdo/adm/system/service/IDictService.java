package com.tecdo.adm.system.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.system.entity.Dict;
import com.tecdo.adm.api.system.vo.DictVO;
import com.tecdo.starter.mp.support.PQuery;

import java.util.List;
import java.util.Map;

/**
 * Created by Zeki on 2023/3/9
 */
public interface IDictService extends IService<Dict> {

	/**
	 * 新增或修改
	 *
	 * @param dict
	 * @return
	 */
	boolean submit(Dict dict);

	/**
	 * 删除字典
	 *
	 * @param ids
	 * @return
	 */
	boolean removeDict(String ids);

	/**
	 * 自定义分页
	 *
	 * @param page
	 * @param dict
	 * @return
	 */
	IPage<DictVO> selectDictPage(IPage<DictVO> page, DictVO dict);

	/**
	 * 下拉框字典树
	 *
	 * @return
	 */
	List<DictVO> tree();

	/**
	 * 顶级字典树
	 *
	 * @return
	 */
	List<DictVO> parentTree();

	/**
	 * 获取字典表对应中文
	 *
	 * @param code    字典编号
	 * @param dictKey 字典序号
	 * @return
	 */
	String getValue(String code, String dictKey);

	/**
	 * 获取字典表
	 *
	 * @param code 字典编号
	 * @return
	 */
	List<Dict> getList(String code);

	/**
	 * 顶级列表
	 *
	 * @param dict
	 * @param query
	 * @return
	 */
	IPage<DictVO> parentList(Map<String, Object> dict, PQuery query);

	/**
	 * 子列表
	 *
	 * @param dict
	 * @param parentId
	 * @return
	 */
	List<DictVO> childList(Map<String, Object> dict, Integer parentId);

}
