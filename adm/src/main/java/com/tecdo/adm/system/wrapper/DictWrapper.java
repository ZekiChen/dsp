package com.tecdo.adm.system.wrapper;

import com.tecdo.adm.api.system.entity.Dict;
import com.tecdo.adm.api.system.vo.DictVO;
import com.tecdo.adm.common.cache.DictCache;
import com.tecdo.adm.common.constant.AdmConstant;
import com.tecdo.starter.mp.support.EntityWrapper;
import com.tecdo.starter.tool.BigTool;
import com.tecdo.starter.tool.node.ForestNodeMerger;
import com.tecdo.starter.tool.util.BeanUtil;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DictWrapper extends EntityWrapper<Dict, DictVO> {

	public static DictWrapper build() {
		return new DictWrapper();
	}

	@Override
	public DictVO entityVO(Dict dict) {
		DictVO dictVO = Objects.requireNonNull(BeanUtil.copy(dict, DictVO.class));
		if (BigTool.equals(dict.getParentId(), AdmConstant.TOP_PARENT_ID)) {
			dictVO.setParentName(AdmConstant.TOP_PARENT_NAME);
		} else {
			Dict parent = DictCache.getById(dict.getParentId());
			dictVO.setParentName(parent.getDictValue());
		}
		return dictVO;
	}

	public List<DictVO> listNodeVO(List<Dict> list) {
		List<DictVO> collect = list.stream().map(dict -> BeanUtil.copy(dict, DictVO.class)).collect(Collectors.toList());
		return ForestNodeMerger.merge(collect);
	}

}