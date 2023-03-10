package com.tecdo.adm.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.system.entity.Dict;
import com.tecdo.adm.api.system.mapper.DictMapper;
import com.tecdo.adm.api.system.vo.DictVO;
import com.tecdo.adm.common.cache.DictCache;
import com.tecdo.adm.common.constant.AdmConstant;
import com.tecdo.adm.system.service.IDictService;
import com.tecdo.adm.system.wrapper.DictWrapper;
import com.tecdo.starter.log.exception.ServiceException;
import com.tecdo.starter.mp.support.PCondition;
import com.tecdo.starter.mp.support.PQuery;
import com.tecdo.starter.redis.CacheUtil;
import com.tecdo.starter.tool.BigTool;
import com.tecdo.starter.tool.node.ForestNodeMerger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tecdo.common.constant.CacheConstant.DICT_CACHE;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements IDictService {

	@Override
	public IPage<DictVO> selectDictPage(IPage<DictVO> page, DictVO dict) {
		return page.setRecords(baseMapper.selectDictPage(page, dict));
	}

	@Override
	public List<DictVO> tree() {
		return ForestNodeMerger.merge(baseMapper.tree());
	}

	@Override
	public List<DictVO> parentTree() {
		return ForestNodeMerger.merge(baseMapper.parentTree());
	}

	@Override
	public String getValue(String code, String dictKey) {
		return BigTool.toStr(baseMapper.getValue(code, dictKey), StrUtil.EMPTY);
	}

	@Override
	public List<Dict> getList(String code) {
		return baseMapper.getList(code);
	}

	@Override
	public boolean submit(Dict dict) {
		LambdaQueryWrapper<Dict> lqw = Wrappers.<Dict>query().lambda().eq(Dict::getCode, dict.getCode()).eq(Dict::getDictKey, dict.getDictKey());
		Long cnt = baseMapper.selectCount((BigTool.isEmpty(dict.getId())) ? lqw : lqw.notIn(Dict::getId, dict.getId()));
		if (cnt > 0L) {
			throw new ServiceException("当前字典键值已存在!");
		}
		// 修改顶级字典后同步更新下属字典的编号
		if (BigTool.isNotEmpty(dict.getId()) && dict.getParentId().longValue() == AdmConstant.TOP_PARENT_ID) {
			Dict parent = DictCache.getById(dict.getId());
			this.update(Wrappers.<Dict>update().lambda().set(Dict::getCode, dict.getCode()).eq(Dict::getCode, parent.getCode()).ne(Dict::getParentId, AdmConstant.TOP_PARENT_ID));
		}
		if (BigTool.isEmpty(dict.getParentId())) {
			dict.setParentId(AdmConstant.TOP_PARENT_ID);
		}
		CacheUtil.clear(DICT_CACHE);
		return saveOrUpdate(dict);
	}

	@Override
	public boolean removeDict(String ids) {
		Long cnt = baseMapper.selectCount(Wrappers.<Dict>query().lambda().in(Dict::getParentId, BigTool.toLongList(ids)));
		if (cnt > 0L) {
			throw new ServiceException("请先删除子节点!");
		}
		return removeByIds(BigTool.toLongList(ids));
	}

	@Override
	public IPage<DictVO> parentList(Map<String, Object> dict, PQuery query) {
		IPage<Dict> page = this.page(PCondition.getPage(query), PCondition.getQueryWrapper(dict, Dict.class).lambda().eq(Dict::getParentId, AdmConstant.TOP_PARENT_ID).orderByAsc(Dict::getSort));
		return DictWrapper.build().pageVO(page);
	}

	@Override
	public List<DictVO> childList(Map<String, Object> dict, Integer parentId) {
		if (parentId < 0) {
			return new ArrayList<>();
		}
		dict.remove("parentId");
		Dict parentDict = DictCache.getById(parentId);
		List<Dict> list = this.list(PCondition.getQueryWrapper(dict, Dict.class).lambda().ne(Dict::getId, parentId).eq(Dict::getCode, parentDict.getCode()).orderByAsc(Dict::getSort));
		return DictWrapper.build().listNodeVO(list);
	}
}