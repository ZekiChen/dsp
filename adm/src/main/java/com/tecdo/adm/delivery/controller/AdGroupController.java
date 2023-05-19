package com.tecdo.adm.delivery.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.vo.AdGroupVO;
import com.tecdo.adm.api.delivery.vo.BatchAdGroupUpdateVO;
import com.tecdo.adm.api.delivery.vo.BundleAdGroupUpdateVO;
import com.tecdo.adm.api.delivery.vo.SimpleAdGroupUpdateVO;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public R<IPage<AdGroupVO>> page(@ApiParam("广告主ID集") @RequestParam(required = false) String advIds,
                                    @ApiParam("广告计划ID集") @RequestParam(required = false) String campaignIds,
                                    @ApiParam("筛选广告计划ID集") @RequestParam(required = false) String cIds,
                                    @ApiParam("筛选广告计划名称") @RequestParam(required = false) String cName,
                                    @ApiParam("筛选广告ID集") @RequestParam(required = false) String adIds,
                                    @ApiParam("筛选广告名称") @RequestParam(required = false) String adName,
                                    @ApiParam("渠道名称") @RequestParam(required = false) String affiliateIds,
                                    @ApiParam("国家三位码") @RequestParam(required = false) String countries,
                                    AdGroup adGroup, PQuery query) {
        List<Integer> preCIds = new ArrayList<>();
        if (StrUtil.isNotBlank(campaignIds)) {
            preCIds.addAll(BigTool.toIntList(campaignIds));
        } else if (StrUtil.isNotBlank(advIds)) {
            preCIds.addAll(service.listIdByAdvIds(BigTool.toIntList(advIds)));
        }
        IPage<AdGroup> pages;
        if (CollUtil.isEmpty(preCIds)) {
            pages = service.customPage(PCondition.getPage(query), adGroup,
                    BigTool.toIntList(cIds), cName,
                    BigTool.toIntList(adIds), adName,
                    BigTool.toStrList(affiliateIds),
                    BigTool.toStrList(countries));
        } else {
            List<Integer> queryCIds = new ArrayList<>();
            if (StrUtil.isEmpty(cIds)) {
                queryCIds.addAll(preCIds);
            } else {
                Set<Integer> set = new HashSet<>(preCIds);
                queryCIds.addAll(BigTool.toIntList(cIds).stream().filter(set::contains).collect(Collectors.toList()));
                if (CollUtil.isEmpty(queryCIds)) {
                    return R.data(new Page<>());
                }
            }
            pages = service.customPage(PCondition.getPage(query), adGroup,
                    queryCIds, cName,
                    BigTool.toIntList(adIds), adName,
                    BigTool.toStrList(affiliateIds),
                    BigTool.toStrList(countries));
        }
        return R.data(AdGroupWrapper.build().pageVO(pages));
    }

    @GetMapping("/list")
    @ApiOperationSupport(order = 6)
    @ApiOperation(value = "列表", notes = "无需传参")
    public R<List<BaseVO>> list() {
        return R.data(service.listIdAndName());
    }

    @PostMapping("/copy")
    @ApiOperationSupport(order = 7)
    @ApiOperation(value = "批量复制", notes = "传入表单参数")
    public R copy(@ApiParam("目标campaignId") @RequestParam Integer targetCampaignId,
                  @ApiParam("源adGroupId") @RequestParam Integer sourceAdGroupId,
                  @ApiParam("复制数量") @RequestParam Integer copyNum,
                  @ApiParam("目标adGroup状态") @RequestParam Integer targetAdGroupStatus,
                  @ApiParam("目标ad状态") @RequestParam Integer targetAdStatus) {
        return R.status(service.copy(targetCampaignId, sourceAdGroupId, copyNum, targetAdGroupStatus, targetAdStatus));
    }

    @PutMapping("/update/list-info")
    @ApiOperationSupport(order = 8)
    @ApiOperation(value = "列表直接修改", notes = "传入SimpleAdGroupUpdateVO")
    public R updateListInfo(@RequestBody SimpleAdGroupUpdateVO vo) {
        CacheUtil.clear(AD_GROUP_CACHE);
        return R.status(service.editListInfo(vo));
    }

    @PutMapping("/update-bundle")
    @ApiOperationSupport(order = 9)
    @ApiOperation(value = "列表直接修改bundle", notes = "传入Object")
    public R updateBundles(@Valid @RequestBody TargetCondition condition) {
        return R.status(service.updateBundles(condition));
    }

    @GetMapping("/bundle/{adGroupId}")
    @ApiOperationSupport(order = 10)
    @ApiOperation(value = "bundle详情", notes = "传入adGroupId")
    public R<TargetCondition> listBundle(@PathVariable("adGroupId") Integer adGroupId) {
        return R.data(service.listBundle(adGroupId));
    }

    @PutMapping("/update-batch")
    @ApiOperationSupport(order = 11)
    @ApiOperation(value = "批量修改", notes = "传入Object")
    public R updateBatch(@Valid @RequestBody BatchAdGroupUpdateVO vo) {
        return R.status(service.updateBatch(vo));
    }

    @PutMapping("/bundle-update-batch")
    @ApiOperationSupport(order = 12)
    @ApiOperation(value = "批量修改bundle", notes = "传入Object")
    public R bundleUpdateBatch(@Valid @RequestBody BundleAdGroupUpdateVO vo) {
        return R.status(service.bundleUpdateBatch(vo));
    }
}
