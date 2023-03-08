package com.tecdo.adm.delivery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.tecdo.adm.delivery.service.IAdService;
import com.tecdo.adm.delivery.wrapper.AdWrapper;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.vo.AdVO;
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
@RequestMapping(AppConstant.ADM + "/ad")
@RestController
@Api(value = "广告", tags = "广告")
@RequiredArgsConstructor
public class AdController {

    private final IAdService service;

    @PostMapping("/save")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "新增", notes = "传入ad")
    public R save(@Valid @RequestBody Ad ad) {
        return R.status(service.save(ad));
    }

    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "修改", notes = "传入ad")
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
    public R<IPage<AdVO>> page(Ad ad, PQuery query) {
        IPage<Ad> pages = service.page(PCondition.getPage(query), PCondition.getQueryWrapper(ad));
        return R.data(AdWrapper.build().pageVO(pages));
    }
}
