package com.tecdo.adm.delivery.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.vo.AdVO;
import com.tecdo.adm.api.delivery.vo.BatchAdUpdateVO;
import com.tecdo.adm.api.delivery.vo.SimpleAdUpdateVO;
import com.tecdo.adm.api.delivery.vo.SimpleAdVO;
import com.tecdo.adm.delivery.service.IAdGroupService;
import com.tecdo.adm.delivery.service.IAdService;
import com.tecdo.adm.delivery.service.ICreativeService;
import com.tecdo.adm.delivery.wrapper.AdWrapper;
import com.tecdo.common.constant.AppConstant;
import com.tecdo.core.launch.response.R;
import com.tecdo.starter.mp.enums.BaseStatusEnum;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tecdo.common.constant.CacheConstant.AD_CACHE;

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
    private final ICreativeService creativeService;

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
        CacheUtil.clear(AD_CACHE);
        return R.status(service.edit(ad));
    }

    @DeleteMapping("/remove")
    @ApiOperationSupport(order = 3)
    @ApiOperation(value = "删除", notes = "传入ids")
    public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
        CacheUtil.clear(AD_CACHE);
        return R.status(service.logicDelete(BigTool.toIntList(ids)));
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
    public R<IPage<AdVO>> page(@ApiParam("广告主ID集") @RequestParam(required = false) String advIds,
                               @ApiParam("广告活动ID集") @RequestParam(required = false) String campaignIds,
                               @ApiParam("广告组ID集") @RequestParam(required = false) String groupIds,
                               @ApiParam("筛选campaignIds") @RequestParam(required = false) String cIds,
                               @ApiParam("筛选campaignName") @RequestParam(required = false) String cName,
                               @ApiParam("筛选adGroupIds") @RequestParam(required = false) String gIds,
                               @ApiParam("筛选adGroupName") @RequestParam(required = false) String gName,
                               @ApiParam("筛选width") @RequestParam(required = false) Integer width,
                               @ApiParam("筛选height") @RequestParam(required = false) Integer height,
                               @ApiParam("筛选creativeId") @RequestParam(required = false) String creativeIds,
                               @ApiParam("筛选creativeName") @RequestParam(required = false) String creativeName,
                               Ad ad, PQuery query) {
        LambdaQueryWrapper<Ad> wrapper = Wrappers.lambdaQuery(ad);
        List<Integer> adGroupIds = adGroupService.listAdGroupIdForListAd(cIds, gIds, cName, gName);
        if (adGroupIds == null) {
            return R.data(new Page<>());
        } else {
            if (CollUtil.isEmpty(adGroupIds)) {
                if (StrUtil.isNotBlank(groupIds)) {
                    wrapper.in(Ad::getGroupId, BigTool.toIntList(groupIds));
                } else if (StrUtil.isNotBlank(campaignIds)) {
                    List<AdGroup> adGroups = adGroupService.listByCampaignIds(BigTool.toIntList(campaignIds));
                    if (CollUtil.isEmpty(adGroups)) {
                        return R.data(new Page<>());
                    }
                    adGroupIds = adGroups.stream().map(AdGroup::getId).collect(Collectors.toList());
                    wrapper.in(Ad::getGroupId, adGroupIds);
                } else if (StrUtil.isNotBlank(advIds)) {
                    List<Integer> groupIdsByAdvIds = adGroupService.listIdByAdvIds(BigTool.toIntList(advIds));
                    if (CollUtil.isEmpty(groupIdsByAdvIds)) {
                        return R.data(new Page<>());
                    }
                    wrapper.in(Ad::getGroupId, groupIdsByAdvIds);
                }
            } else {
                if (StrUtil.isNotBlank(groupIds)) {
                    Set<Integer> set = new HashSet<>(BigTool.toIntList(groupIds));
                    adGroupIds = adGroupIds.stream().filter(set::contains).collect(Collectors.toList());
                    if (CollUtil.isEmpty(adGroupIds)) {
                        return R.data(new Page<>());
                    }
                    wrapper.in(Ad::getGroupId, adGroupIds);
                } else if (StrUtil.isNotBlank(campaignIds)) {
                    List<AdGroup> adGroups = adGroupService.listByCampaignIds(BigTool.toIntList(campaignIds));
                    if (CollUtil.isNotEmpty(adGroups)) {
                        Set<Integer> set = adGroups.stream().map(AdGroup::getId).collect(Collectors.toSet());
                        adGroupIds = adGroupIds.stream().filter(set::contains).collect(Collectors.toList());
                        if (CollUtil.isEmpty(adGroupIds)) {
                            return R.data(new Page<>());
                        }
                        wrapper.in(Ad::getGroupId, adGroupIds);
                    }
                } else if (StrUtil.isNotBlank(advIds)) {
                    List<Integer> groupIdsByAdvIds = adGroupService.listIdByAdvIds(BigTool.toIntList(advIds));
                    if (CollUtil.isEmpty(groupIdsByAdvIds)) {
                        return R.data(new Page<>());
                    }
                    Set<Integer> set = new HashSet<>(groupIdsByAdvIds);
                    adGroupIds = adGroupIds.stream().filter(set::contains).collect(Collectors.toList());
                    wrapper.in(Ad::getGroupId, adGroupIds);
                } else {
                    wrapper.in(Ad::getGroupId, adGroupIds);
                }
            }
        }
        if (ad.getStatus() == null) {
            wrapper.ne(Ad::getStatus, BaseStatusEnum.DELETE.getType());
        }

        List<Integer> creativeIdList = new ArrayList<>();
        if (StrUtil.isNotBlank(creativeIds)) {
            creativeIdList.addAll(BigTool.toIntList(creativeIds));
        }
        if (StrUtil.isNotBlank(creativeName)) {
            List<Integer> IdsByLikeName = creativeService.listIdByLikeName(creativeName);
            if (CollUtil.isEmpty(creativeIdList)) {
                creativeIdList.addAll(IdsByLikeName);
            } else {
                Set<Integer> set = new HashSet<>(IdsByLikeName);
                creativeIdList = creativeIdList.stream().filter(set::contains).collect(Collectors.toList());
            }
            if (CollUtil.isEmpty(creativeIdList)) {
                return R.data(new Page<>());
            }
        }
        if (width != null && height != null) {
            List<Integer> idsBySize = creativeService.listIdBySize(width, height);
            if (CollUtil.isEmpty(creativeIdList)) {
                creativeIdList.addAll(idsBySize);
            } else {
                Set<Integer> set = new HashSet<>(idsBySize);
                creativeIdList = creativeIdList.stream().filter(set::contains).collect(Collectors.toList());
            }
            if (CollUtil.isEmpty(creativeIdList)) {
                return R.data(new Page<>());
            }
        }
        creativeIdList = creativeIdList.stream().distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(creativeIdList)) {
            wrapper.in(Ad::getImage, creativeIdList)
                    .or()
                    .in(Ad::getIcon, creativeIdList)
                    .or()
                    .in(Ad::getVideo, creativeIdList);
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

    @PostMapping("/copy")
    @ApiOperationSupport(order = 7)
    @ApiOperation(value = "批量复制", notes = "传入表单参数")
    public R copy(@ApiParam("源adId集") @RequestParam String sourceAdIds,
                  @ApiParam("目标adGroupId集") @RequestParam String targetAdGroupIds,
                  @ApiParam("目标ad状态") @RequestParam Integer targetAdStatus) {
        return R.status(service.copy(sourceAdIds, BigTool.toIntList(targetAdGroupIds), targetAdStatus));
    }

    @PutMapping("/update/list-info")
    @ApiOperationSupport(order = 8)
    @ApiOperation(value = "列表直接修改", notes = "传入SimpleAdGroupUpdateVO")
    public R updateListInfo(@RequestBody SimpleAdUpdateVO vo) {
        CacheUtil.clear(AD_CACHE);
        return R.status(service.editListInfo(vo));
    }

    @PutMapping("/update-batch")
    @ApiOperationSupport(order = 9)
    @ApiOperation(value = "批量修改", notes = "传入Object")
    public R updateBatch(@Valid @RequestBody BatchAdUpdateVO vo) {
        CacheUtil.clear(AD_CACHE);
        return R.status(service.updateBatch(vo));
    }

    @GetMapping("/list/{adGroupIds}")
    @ApiOperationSupport(order = 10)
    @ApiOperation(value = "根据gIds获取ad列表", notes = "传入adGroupIds")
    public R<List<SimpleAdVO>> listSimpleAd(@PathVariable String adGroupIds) {
        return R.data(service.listSimpleAd(BigTool.toIntList(adGroupIds)));
    }
}
