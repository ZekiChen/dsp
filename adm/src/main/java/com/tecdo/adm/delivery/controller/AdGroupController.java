package com.tecdo.adm.delivery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.vo.AdGroupVO;
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
                                    @ApiParam("渠道名称") @RequestParam(required = false) String affiliateIds,
                                    AdGroup adGroup, PQuery query) {
        IPage<AdGroup> pages = service.customPage(PCondition.getPage(query), adGroup,
                BigTool.toIntList(campaignIds), BigTool.toStrList(affiliateIds));
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
        return R.status(service.editListInfo(vo.getId(), vo.getOptPrice(), vo.getDailyBudget()));
    }

}
