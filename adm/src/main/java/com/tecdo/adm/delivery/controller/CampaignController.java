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
import com.tecdo.starter.tool.BigTool;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by Zeki on 2023/3/6
 */
@RequestMapping(AppConstant.ADM + "/campaign")
@RestController
@Api(value = "广告活动", tags = "广告活动")
@RequiredArgsConstructor
public class CampaignController {

    private final ICampaignService service;

    @PostMapping("/save")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "新增", notes = "传入campaign")
    public R save(@Valid @RequestBody CampaignVO campaignVO) {
        return R.status(service.add(campaignVO));
    }

    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "修改", notes = "传入campaign")
    public R update(@Valid @RequestBody Campaign campaign) {
        return R.status(service.updateById(campaign));
    }

    @DeleteMapping("/remove")
    @ApiOperationSupport(order = 3)
    @ApiOperation(value = "删除", notes = "传入ids")
    public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
        return R.status(service.removeByIds(BigTool.toLongList(ids)));
    }

    @GetMapping("/detail")
    @ApiOperationSupport(order = 4)
    @ApiOperation(value = "详情", notes = "传入campaign")
    public R<CampaignVO> detail(Campaign campaign) {
        Campaign detail = service.getOne(PCondition.getQueryWrapper(campaign));
        return R.data(CampaignWrapper.build().entityVO(detail));
    }

    @GetMapping("/page")
    @ApiOperationSupport(order = 5)
    @ApiOperation(value = "分页", notes = "传入campaign")
    public R<IPage<CampaignVO>> page(Campaign campaign, PQuery query) {
        IPage<Campaign> pages = service.page(PCondition.getPage(query), PCondition.getQueryWrapper(campaign));
        return R.data(CampaignWrapper.build().pageVO(pages));
    }
}
