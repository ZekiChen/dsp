package com.tecdo.adm.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.tecdo.adm.api.system.entity.Dict;
import com.tecdo.adm.api.system.vo.DictVO;
import com.tecdo.adm.system.service.IDictService;
import com.tecdo.adm.system.wrapper.DictWrapper;
import com.tecdo.core.launch.response.R;
import com.tecdo.starter.mp.support.PCondition;
import com.tecdo.starter.mp.support.PQuery;
import com.tecdo.starter.redis.CacheUtil;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static com.tecdo.common.constant.CacheConstant.DICT_CACHE;

/**
 * Created by Zeki on 2023/3/9
 */
@RestController
@AllArgsConstructor
@RequestMapping("/dict")
@ApiSupport(order = 1)
@Api(tags = "字典")
@CrossOrigin(origins = "*")
public class DictController {

	private final IDictService service;

	@PostMapping("/submit")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "新增或修改", notes = "传入Dict")
	public R submit(@Valid @RequestBody Dict dict) {
		CacheUtil.clear(DICT_CACHE);
		return R.status(service.submit(dict));
	}

	@DeleteMapping("/remove")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "删除", notes = "传入ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		CacheUtil.clear(DICT_CACHE);
		return R.status(service.removeDict(ids));
	}

	@GetMapping("/detail")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "详情", notes = "传入Dict")
	public R<DictVO> detail(Dict dict) {
		Dict detail = service.getOne(PCondition.getQueryWrapper(dict));
		return R.data(DictWrapper.build().entityVO(detail));
	}

	@GetMapping("/list")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "code", value = "字典编号", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "dictValue", value = "字典名称", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "列表", notes = "传入dict")
	public R<List<DictVO>> list(@ApiIgnore @RequestParam Map<String, Object> dict) {
		List<Dict> list = service.list(PCondition.getQueryWrapper(dict, Dict.class).lambda().orderByAsc(Dict::getId));
		DictWrapper dictWrapper = new DictWrapper();
		return R.data(dictWrapper.listNodeVO(list));
	}

	@GetMapping("/parent-list")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "code", value = "字典编号", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "dictValue", value = "字典名称", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "顶级列表", notes = "传入dict")
	public R<IPage<DictVO>> parentList(@ApiIgnore @RequestParam Map<String, Object> dict, PQuery query) {
		return R.data(service.parentList(dict, query));
	}

	@GetMapping("/child-list")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "code", value = "字典编号", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "dictValue", value = "字典名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "parentId", value = "字典名称", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "子列表", notes = "传入dict")
	public R<List<DictVO>> childList(@ApiIgnore @RequestParam Map<String, Object> dict,
									 @RequestParam(required = false, defaultValue = "-1") Integer parentId) {
		return R.data(service.childList(dict, parentId));
	}

	@GetMapping("/tree")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "下拉框字典树", notes = "无需传参")
	public R<List<DictVO>> tree() {
		List<DictVO> tree = service.tree();
		return R.data(tree);
	}

	@GetMapping("/parent-tree")
	@ApiOperationSupport(order = 8)
	@ApiOperation(value = "顶级字典树", notes = "无需传参")
	public R<List<DictVO>> parentTree() {
		List<DictVO> tree = service.parentTree();
		return R.data(tree);
	}

	@GetMapping("/dictionary")
	@ApiOperationSupport(order = 9)
	@ApiOperation(value = "根据code获取字典列表", notes = "传入code")
	public R<List<Dict>> dict(String code) {
		List<Dict> tree = service.getList(code);
		return R.data(tree);
	}

	@GetMapping("/dictionary-tree")
	@ApiOperationSupport(order = 10)
	@ApiOperation(value = "根据code获取字典树", notes = "传入code")
	public R<List<DictVO>> dictTree(String code) {
		List<Dict> tree = service.getList(code);
		return R.data(DictWrapper.build().listNodeVO(tree));
	}

}

