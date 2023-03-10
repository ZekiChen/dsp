package com.tecdo.adm.delivery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.vo.AdGroupVO;
import com.tecdo.adm.delivery.service.IAdGroupService;
import com.tecdo.adm.delivery.wrapper.AdGroupWrapper;
import com.tecdo.common.constant.AppConstant;
import com.tecdo.core.launch.response.R;
import com.tecdo.starter.mp.support.PCondition;
import com.tecdo.starter.mp.support.PQuery;
import com.tecdo.starter.redis.CacheUtil;
import com.tecdo.starter.tool.BigTool;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.tecdo.common.constant.CacheConstant.CAMPAIGN_CACHE;

/**
 * Created by Zeki on 2023/3/6
 */
@RequestMapping(AppConstant.ADM + "/ad-group")
@RestController
@Api(tags = "广告组")
@RequiredArgsConstructor
public class AdGroupController {

    private final IAdGroupService service;

    @PostMapping("/add")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "新增", notes = "传入AdGroupVO")
    public R save(@Valid @RequestBody AdGroupVO vo) {
        return R.status(service.add(vo));
    }

    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "修改", notes = "传入AdGroupVO")
    public R update(@Valid @RequestBody AdGroupVO vo) {
        CacheUtil.clear(CAMPAIGN_CACHE);
        return R.status(service.edit(vo));
    }

    @DeleteMapping("/remove")
    @ApiOperationSupport(order = 3)
    @ApiOperation(value = "删除", notes = "传入ids")
    public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
        CacheUtil.clear(CAMPAIGN_CACHE);
        return R.status(service.delete(BigTool.toIntList(ids)));
    }

    @GetMapping("/detail")
    @ApiOperationSupport(order = 4)
    @ApiOperation(value = "详情", notes = "传入AdGroup")
    public R<AdGroupVO> detail(AdGroup adGroup) {
        AdGroup detail = service.getOne(PCondition.getQueryWrapper(adGroup));
        return R.data(AdGroupWrapper.build().entityVO(detail));
    }

    @GetMapping("/page")
    @ApiOperationSupport(order = 5)
    @ApiOperation(value = "分页", notes = "传入AdGroup")
    public R<IPage<AdGroupVO>> page(AdGroup adGroup, PQuery query) {
        IPage<AdGroup> pages = service.page(PCondition.getPage(query), PCondition.getQueryWrapper(adGroup));
        return R.data(AdGroupWrapper.build().pageVO(pages));
    }
}
