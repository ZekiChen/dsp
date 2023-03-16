package com.tecdo.adm.delivery.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.vo.AdVO;
import com.tecdo.adm.delivery.service.IAdGroupService;
import com.tecdo.adm.delivery.service.IAdService;
import com.tecdo.adm.delivery.wrapper.AdWrapper;
import com.tecdo.common.constant.AppConstant;
import com.tecdo.core.launch.response.R;
import com.tecdo.starter.mp.support.PCondition;
import com.tecdo.starter.mp.support.PQuery;
import com.tecdo.starter.tool.BigTool;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/3/6
 */
@RequestMapping(AppConstant.ADM + "/ad")
@RestController
@Api(tags = "广告")
@RequiredArgsConstructor
public class AdController {

    private final IAdService service;
    private final IAdGroupService adGroupService;

    @PostMapping("/add")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "新增", notes = "传入Ad")
    public R save(@Valid @RequestBody Ad ad) {
        return R.status(service.save(ad));
    }

    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "修改", notes = "传入Ad")
    public R update(@Valid @RequestBody Ad ad) {
        return R.status(service.updateById(ad));
    }

    @DeleteMapping("/remove")
    @ApiOperationSupport(order = 3)
    @ApiOperation(value = "删除", notes = "传入ids")
    public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
        return R.status(service.removeByIds(BigTool.toLongList(ids)));
    }

    @GetMapping("/detail")
    @ApiOperationSupport(order = 4)
    @ApiOperation(value = "详情", notes = "传入ad")
    public R<AdVO> detail(Ad ad) {
        Ad detail = service.getOne(PCondition.getQueryWrapper(ad));
        return R.data(AdWrapper.build().entityVO(detail));
    }

    @GetMapping("/page")
    @ApiOperationSupport(order = 5)
    @ApiOperation(value = "分页", notes = "传入ad")
    public R<IPage<AdVO>> page(@ApiParam("广告活动ID集") @RequestParam(required = false) String campaignIds,
                               @ApiParam("广告组ID集") @RequestParam(required = false) String groupIds,
                               Ad ad, PQuery query) {
        LambdaQueryWrapper<Ad> wrapper = Wrappers.lambdaQuery(ad);
        if (StrUtil.isNotBlank(campaignIds)) {
            List<AdGroup> adGroups = adGroupService.listByIds(BigTool.toIntList(campaignIds));
            if (CollUtil.isNotEmpty(adGroups)) {
                List<Integer> adGroupIds = adGroups.stream().map(AdGroup::getId).collect(Collectors.toList());
                wrapper.in(Ad::getGroupId, adGroupIds);
            }
        } else if (StrUtil.isNotBlank(groupIds)) {
            wrapper.in(Ad::getGroupId, BigTool.toIntList(groupIds));
        }
        IPage<Ad> pages = service.page(PCondition.getPage(query), wrapper);
        return R.data(AdWrapper.build().pageVO(pages));
    }

    @PostMapping("/add-batch")
    @ApiOperationSupport(order = 6)
    @ApiOperation(value = "批量新增", notes = "传入Ad集")
    public R save(@Valid @RequestBody List<Ad> ads) {
        return R.status(service.saveBatch(ads));
    }
}
