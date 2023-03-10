package com.tecdo.adm.delivery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.tecdo.adm.api.delivery.entity.Campaign;
import com.tecdo.adm.api.delivery.vo.CampaignVO;
import com.tecdo.adm.delivery.service.ICampaignService;
import com.tecdo.adm.delivery.wrapper.CampaignWrapper;
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
@RequestMapping(AppConstant.ADM + "/campaign")
@RestController
@Api(tags = "广告活动")
@RequiredArgsConstructor
public class CampaignController {

    private final ICampaignService service;

    @PostMapping("/add")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "新增", notes = "传入campaignVO")
    public R save(@Valid @RequestBody CampaignVO vo) {
        return R.status(service.add(vo));
    }

    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "修改", notes = "传入CampaignVO")
    public R update(@Valid @RequestBody CampaignVO vo) {
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
    @ApiOperation(value = "详情", notes = "传入Campaign")
    public R<CampaignVO> detail(Campaign campaign) {
        Campaign detail = service.getOne(PCondition.getQueryWrapper(campaign));
        return R.data(CampaignWrapper.build().entityVO(detail));
    }

    @GetMapping("/page")
    @ApiOperationSupport(order = 5)
    @ApiOperation(value = "分页", notes = "传入Campaign")
    public R<IPage<CampaignVO>> page(Campaign campaign, PQuery query) {
        IPage<Campaign> pages = service.page(PCondition.getPage(query), PCondition.getQueryWrapper(campaign));
        return R.data(CampaignWrapper.build().pageVO(pages));
    }
}
