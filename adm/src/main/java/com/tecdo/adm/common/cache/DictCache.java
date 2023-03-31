package com.tecdo.adm.common.cache;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.tecdo.adm.api.system.entity.Dict;
import com.tecdo.adm.api.system.enums.DictEnum;
import com.tecdo.adm.system.service.IDictService;
import com.tecdo.starter.redis.CacheUtil;

import java.util.List;
import java.util.Optional;

import static com.tecdo.common.constant.CacheConstant.DICT_CACHE;

public class DictCache {

	private static final String DICT_ID = "dict:id:";
	private static final String DICT_KEY = "dict:key:";
	private static final String DICT_VALUE = "dict:value:";
	private static final String DICT_LIST = "dict:list:";

	private static final IDictService dictService = SpringUtil.getBean(IDictService.class);

	/**
	 * 获取字典实体
	 *
	 * @param id 主键
	 * @return Dict
	 */
	public static Dict getById(Integer id) {
		return CacheUtil.get(DICT_CACHE, DICT_ID, id, () -> dictService.getById(id));
	}

	/**
	 * 获取字典值
	 *
	 * @param code      字典编号枚举
	 * @param dictValue 字典值
	 * @return String
	 */
	public static String getKey(DictEnum code, String dictValue) {
		return getKey(code.getName(), dictValue);
	}

	/**
	 * 获取字典键
	 *
	 * @param code      字典编号
	 * @param dictValue 字典值
	 * @return String
	 */
	public static String getKey(String code, String dictValue) {
		return CacheUtil.get(DICT_CACHE, DICT_KEY + code + StrUtil.COLON, dictValue, () -> {
			List<Dict> list = getList(code);
			Optional<String> key = list.stream().filter(
				dict -> dict.getDictValue().equalsIgnoreCase(dictValue)
			).map(Dict::getDictKey).findFirst();
			return key.orElse(StrUtil.EMPTY);
		});
	}

	/**
	 * 获取字典值
	 *
	 * @param code    字典编号枚举
	 * @param dictKey Integer型字典键
	 * @return String
	 */
	public static String getValue(DictEnum code, Integer dictKey) {
		return getValue(code.getName(), dictKey);
	}

	/**
	 * 获取字典值
	 *
	 * @param code    字典编号
	 * @param dictKey Integer型字典键
	 * @return String
	 */
	public static String getValue(String code, Integer dictKey) {
		return CacheUtil.get(DICT_CACHE, DICT_VALUE + code + StrUtil.COLON, String.valueOf(dictKey), () ->
				dictService.getValue(code, String.valueOf(dictKey)));
	}

	/**
	 * 获取字典值
	 *
	 * @param code    字典编号枚举
	 * @param dictKey String型字典键
	 * @return String
	 */
	public static String getValue(DictEnum code, String dictKey) {
		return getValue(code.getName(), dictKey);
	}

	/**
	 * 获取字典值
	 *
	 * @param code    字典编号
	 * @param dictKey String型字典键
	 * @return String
	 */
	public static String getValue(String code, String dictKey) {
		return CacheUtil.get(DICT_CACHE, DICT_VALUE + code + StrUtil.COLON, dictKey, () ->
				dictService.getValue(code, dictKey));
	}

	/**
	 * 获取字典集合
	 *
	 * @param code 字典编号
	 * @return List<Dict>
	 */
	public static List<Dict> getList(String code) {
		return CacheUtil.get(DICT_CACHE, DICT_LIST, code, () -> dictService.getList(code));
	}

}