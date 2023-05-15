package com.tecdo.adm.delivery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.tecdo.adm.api.delivery.entity.Campaign;
import com.tecdo.adm.api.delivery.vo.BaseCampaignVO;
import com.tecdo.adm.api.delivery.vo.CampaignVO;
import com.tecdo.adm.api.delivery.vo.SimpleCampaignUpdateVO;
import com.tecdo.adm.delivery.service.ICampaignService;
import com.tecdo.adm.delivery.wrapper.CampaignWrapper;
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
    public R<Integer> save(@Valid @RequestBody CampaignVO vo) {
        return service.add(vo) ? R.data(vo.getId()) : R.failure();
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
        CacheUtil.clear(AD_GROUP_CACHE);
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
    public R<IPage<CampaignVO>> page(@ApiParam("广告主ID集") @RequestParam(required = false) String advIds,
                                     @ApiParam("广告组ID集") @RequestParam(required = false) String adGroupIds,
                                     @ApiParam("广告组名称") @RequestParam(required = false) String adGroupName,
                                     @ApiParam("广告ID集") @RequestParam(required = false) String adIds,
                                     @ApiParam("广告名称") @RequestParam(required = false) String adName,
                                     Campaign campaign, PQuery query) {
        IPage<Campaign> pages = service.customPage(PCondition.getPage(query), campaign,
                BigTool.toIntList(advIds),
                BigTool.toIntList(adGroupIds), adGroupName,
                BigTool.toIntList(adIds), adName);
        return R.data(CampaignWrapper.build().pageVO(pages));
    }

    @GetMapping("/list")
    @ApiOperationSupport(order = 6)
    @ApiOperation(value = "列表", notes = "无需传参")
    public R<List<BaseVO>> list() {
        return R.data(service.listIdAndName());
    }

    @GetMapping("/list-with-group")
    @ApiOperationSupport(order = 7)
    @ApiOperation(value = "列表（包含AdGroup)", notes = "无需传参")
    public R<List<BaseCampaignVO>> listWithGroup() {
        return R.data(service.listCampaignWithGroupIdName());
    }

    @PutMapping("/update/list-info")
    @ApiOperationSupport(order = 8)
    @ApiOperation(value = "列表直接修改", notes = "传入SimpleCampaignUpdateVO")
    public R updateListInfo(@RequestBody SimpleCampaignUpdateVO vo) {
        CacheUtil.clear(CAMPAIGN_CACHE);
        return R.status(service.editListInfo(vo));
    }

}
