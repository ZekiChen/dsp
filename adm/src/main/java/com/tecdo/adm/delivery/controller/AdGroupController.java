package com.tecdo.adm.delivery.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.vo.AdGroupVO;
import com.tecdo.adm.delivery.service.IAdGroupService;
import com.tecdo.adm.delivery.wrapper.AdGroupWrapper;
import com.tecdo.common.constant.AppConstant;
import com.tecdo.core.launch.response.R;
import com.tecdo.starter.mp.support.PCondition;
import com.tecdo.starter.mp.support.PQuery;
import com.tecdo.starter.mp.vo.BaseVO;
import com.tecdo.starter.redis.CacheUtil;
import com.tecdo.starter.tool.BigTool;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;

import static com.tecdo.common.constant.CacheConstant.AD_GROUP_CACHE;

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
    public R<Integer> save(@Valid @RequestBody AdGroupVO vo) {
        return service.add(vo) ? R.data(vo.getId()) : R.failure();
    }

    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "修改", notes = "传入AdGroupVO")
    public R update(@Valid @RequestBody AdGroupVO vo) {
        CacheUtil.clear(AD_GROUP_CACHE);
        return R.status(service.edit(vo));
    }

    @DeleteMapping("/remove")
    @ApiOperationSupport(order = 3)
    @ApiOperation(value = "删除", notes = "传入ids")
    public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
        CacheUtil.clear(AD_GROUP_CACHE);
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
    public R<IPage<AdGroupVO>> page(@ApiParam("广告活动ID集") @RequestParam(required = false) String campaignIds,
                                    AdGroup adGroup, PQuery query) {
        LambdaQueryWrapper<AdGroup> wrapper = Wrappers.lambdaQuery(adGroup);
        if (StrUtil.isNotBlank(campaignIds)) {
            wrapper.in(AdGroup::getCampaignId, BigTool.toIntList(campaignIds));
        }
        IPage<AdGroup> pages = service.page(PCondition.getPage(query), wrapper);
        return R.data(AdGroupWrapper.build().pageVO(pages));
    }

    @GetMapping("/list")
    @ApiOperationSupport(order = 6)
    @ApiOperation(value = "列表", notes = "无需传参")
    public R<List<BaseVO>> list() {
        return R.data(service.listIdAndName());
    }
}
