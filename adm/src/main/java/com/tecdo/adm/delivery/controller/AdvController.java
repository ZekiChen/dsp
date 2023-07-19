package com.tecdo.adm.delivery.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.tecdo.adm.api.delivery.entity.Adv;
import com.tecdo.adm.api.delivery.vo.SimpleAdvVO;
import com.tecdo.adm.delivery.service.IAdvService;
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
import java.util.List;

import static com.tecdo.common.constant.CacheConstant.ADV_CACHE;

/**
 * Created by Zeki on 2023/4/5
 */
@RequestMapping(AppConstant.ADM + "/adv")
@RestController
@Api(tags = "广告主")
@RequiredArgsConstructor
public class AdvController {

    private final IAdvService service;

    @PostMapping("/add")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "新增", notes = "传入Adv")
    public R save(@Valid @RequestBody Adv adv) {
        return R.status(service.save(adv));
    }

    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "修改", notes = "传入Adv")
    public R update(@Valid @RequestBody Adv adv) {
        CacheUtil.clear(ADV_CACHE);
        return R.status(service.updateById(adv));
    }

    @DeleteMapping("/remove")
    @ApiOperationSupport(order = 3)
    @ApiOperation(value = "删除", notes = "传入ids")
    public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
        CacheUtil.clear(ADV_CACHE);
        return R.status(service.removeByIds(BigTool.toLongList(ids)));
    }

    @GetMapping("/detail")
    @ApiOperationSupport(order = 4)
    @ApiOperation(value = "详情", notes = "传入Adv")
    public R<Adv> detail(Adv adv) {
        Adv detail = service.getOne(PCondition.getQueryWrapper(adv));
        return R.data(detail);
    }

    @GetMapping("/page")
    @ApiOperationSupport(order = 5)
    @ApiOperation(value = "分页", notes = "传入ad")
    public R<IPage<Adv>> page(Adv adv, PQuery query,
                              @RequestParam(value = "ids", required = false) String ids) {
        LambdaQueryWrapper<Adv> wrapper = Wrappers.lambdaQuery();
        wrapper.in(StrUtil.isNotBlank(ids), Adv::getId, BigTool.toIntList(ids));
        wrapper.like(StrUtil.isNotBlank(adv.getName()), Adv::getName, adv.getName());
        wrapper.eq(adv.getType() != null, Adv::getType, adv.getType());
        wrapper.eq(adv.getStatus() != null, Adv::getStatus, adv.getStatus());
        IPage<Adv> pages = service.page(PCondition.getPage(query), wrapper);
        return R.data(pages);
    }

    @GetMapping("/list")
    @ApiOperationSupport(order = 6)
    @ApiOperation(value = "列表", notes = "无需传参")
    public R<List<SimpleAdvVO>> list() {
        return R.data(service.listIdAndName());
    }

    @GetMapping("/info/{campaignId}")
    @ApiOperationSupport(order = 7)
    @ApiOperation(value = "根据cId获取Adv", notes = "传入campaignId")
    public R<Adv> getByCampaignId(@PathVariable("campaignId") Integer campaignId) {
        return R.data(service.getByCampaignId(campaignId));
    }
}
