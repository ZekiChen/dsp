package com.tecdo.adm.delivery.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.vo.*;
import com.tecdo.adm.api.doris.entity.AdGroupCost;
import com.tecdo.adm.delivery.service.IAdGroupService;
import com.tecdo.adm.delivery.service.ICampaignService;
import com.tecdo.adm.delivery.wrapper.AdGroupWrapper;
import com.tecdo.adm.doris.IImpCostService;
import com.tecdo.common.constant.AppConstant;
import com.tecdo.core.launch.response.R;
import com.tecdo.starter.mp.entity.IdEntity;
import com.tecdo.starter.mp.support.PCondition;
import com.tecdo.starter.mp.support.PQuery;
import com.tecdo.starter.mp.vo.BaseVO;
import com.tecdo.starter.redis.CacheUtil;
import com.tecdo.starter.tool.BigTool;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tecdo.common.constant.CacheConstant.AD_GROUP_CACHE;

/**
 * Created by Zeki on 2023/3/6
 */
@RequestMapping(AppConstant.ADM + "/ad-group")
@RestController
@Api(tags = "广告组")
@RequiredArgsConstructor
@Slf4j
public class AdGroupController {

    private final IAdGroupService service;
    private final ICampaignService campaignService;
    private final IImpCostService impCostService;

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
        return R.status(service.logicDelete(BigTool.toIntList(ids)));
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
            preCIds.addAll(campaignService.listIdByAdvIds(BigTool.toIntList(advIds)));
            if (CollUtil.isEmpty(preCIds)) {
                return R.data(new Page<>());
            }
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
        IPage<AdGroupVO> voPage = AdGroupWrapper.build().pageVO(pages);
        List<AdGroupVO> records = voPage.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            List<Integer> ids = records.stream().map(IdEntity::getId).collect(Collectors.toList());
            List<AdGroupCost> impCosts = impCostService.listByGroupIds(ids);
            Map<String, AdGroupCost> impCostMap = impCosts.stream()
                    .collect(Collectors.toMap(AdGroupCost::getAdGroupId, Function.identity()));
            records.forEach(e -> {
                AdGroupCost impCost = impCostMap.getOrDefault(e.getId().toString(), null);
                e.setCostFull(impCost != null && impCost.getSumSuccessPrice() / 1000 >= e.getDailyBudget());
            });
        }
        return R.data(voPage);
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
                  @ApiParam("源adGroupIds") @RequestParam String sourceAdGroupIds,
                  @ApiParam("复制数量") @RequestParam Integer copyNum,
                  @ApiParam("目标adGroup状态") @RequestParam Integer targetAdGroupStatus) {
        return R.status(service.copy(targetCampaignId, sourceAdGroupIds, copyNum, targetAdGroupStatus));
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
    public R updateBundles(@Valid @RequestBody List<TargetCondition> conditions) {
        CacheUtil.clear(AD_GROUP_CACHE);
        return R.status(service.updateBundles(conditions));
    }

    @GetMapping("/bundle/{adGroupId}")
    @ApiOperationSupport(order = 10)
    @ApiOperation(value = "bundle详情", notes = "传入adGroupId")
    public R<List<TargetCondition>> listBundle(@PathVariable("adGroupId") Integer adGroupId) {
        return R.data(service.listBundle(adGroupId));
    }

    @PutMapping("/update-batch")
    @ApiOperationSupport(order = 11)
    @ApiOperation(value = "批量修改", notes = "传入Object")
    public R updateBatch(@Valid @RequestBody BatchAdGroupUpdateVO vo) {
        CacheUtil.clear(AD_GROUP_CACHE);
        return R.status(service.updateBatch(vo));
    }

    @PutMapping("/bundle-update-batch")
    @ApiOperationSupport(order = 12)
    @ApiOperation(value = "批量修改bundle", notes = "传入Object")
    public R bundleUpdateBatch(@Valid @RequestBody BundleAdGroupUpdateVO vo) {
        CacheUtil.clear(AD_GROUP_CACHE);
        return R.status(service.bundleUpdateBatch(vo));
    }

    @PutMapping("/hour-update-batch")
    @ApiOperationSupport(order = 13)
    @ApiOperation(value = "批量修改投放时段", notes = "传入Object")
    public R hourUpdateBatch(@Valid @RequestBody BundleAdGroupUpdateVO vo) {
        CacheUtil.clear(AD_GROUP_CACHE);
        return R.status(service.hourUpdateBatch(vo));
    }

    @PutMapping("/fqc-update-batch")
    @ApiOperationSupport(order = 14)
    @ApiOperation(value = "批量修改曝光/点击频控", notes = "传入Object")
    public R fqcUpdateBatch(@Valid @RequestBody FqcAdGroupUpdateVO vo) {
        CacheUtil.clear(AD_GROUP_CACHE);
        return R.status(service.fqcUpdateBatch(vo));
    }

    @PostMapping("/count-device")
    @ApiOperationSupport(order = 15)
    @ApiOperation(value = "根据定向条件圈选显示预估覆盖UV", notes = "传入Object")
    public R<String> countDevice(@Valid @RequestBody List<TargetCondition> conditions) {
        long start = System.currentTimeMillis();
        R<String> data = R.data(service.countDevice(conditions));
        log.info("count device cost: {}s", (System.currentTimeMillis() - start) / 1000);
        return data;
    }
}
