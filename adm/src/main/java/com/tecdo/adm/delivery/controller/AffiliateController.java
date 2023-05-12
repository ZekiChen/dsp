package com.tecdo.adm.delivery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.tecdo.adm.api.delivery.entity.AffCountryBundleBList;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.vo.AffiliateVO;
import com.tecdo.adm.delivery.service.IAffiliateService;
import com.tecdo.adm.delivery.wrapper.AffiliateWrapper;
import com.tecdo.common.constant.AppConstant;
import com.tecdo.core.launch.response.R;
import com.tecdo.starter.mp.support.PCondition;
import com.tecdo.starter.mp.support.PQuery;
import com.tecdo.starter.mp.vo.BaseVO;
import com.tecdo.starter.tool.BigTool;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Created by Zeki on 2023/3/15
 */
@RequestMapping(AppConstant.ADM + "/affiliate")
@RestController
@Api(tags = "渠道")
@RequiredArgsConstructor
public class AffiliateController {

    private final IAffiliateService service;

    @PostMapping("/add")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "新增", notes = "传入Affiliate")
    public R save(@Valid @RequestBody Affiliate affiliate) {
        return R.status(service.save(affiliate));
    }

    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "修改", notes = "传入Affiliate")
    public R update(@Valid @RequestBody Affiliate affiliate) {
        return R.status(service.updateById(affiliate));
    }

    @DeleteMapping("/remove")
    @ApiOperationSupport(order = 3)
    @ApiOperation(value = "删除", notes = "传入ids")
    public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
        return R.status(service.removeByIds(BigTool.toLongList(ids)));
    }

    @GetMapping("/detail")
    @ApiOperationSupport(order = 4)
    @ApiOperation(value = "详情", notes = "传入Affiliate")
    public R<AffiliateVO> detail(Affiliate affiliate) {
        Affiliate detail = service.getOne(PCondition.getQueryWrapper(affiliate));
        return R.data(AffiliateWrapper.build().entityVO(detail));
    }

    @GetMapping("/page")
    @ApiOperationSupport(order = 5)
    @ApiOperation(value = "分页", notes = "传入ad")
    public R<IPage<AffiliateVO>> page(Affiliate affiliate, PQuery query) {
        IPage<Affiliate> pages = service.page(PCondition.getPage(query), PCondition.getQueryWrapper(affiliate));
        return R.data(AffiliateWrapper.build().pageVO(pages));
    }

    @GetMapping("/list")
    @ApiOperationSupport(order = 6)
    @ApiOperation(value = "列表", notes = "无需传参")
    public R<List<BaseVO>> list() {
        return R.data(service.listIdAndName());
    }

    @PostMapping("/update-country-bundle-blist")
    @ApiOperationSupport(order = 7)
    @ApiOperation(value = "渠道*国家*bundle黑名单配置", notes = "传入AffCountryBundleBList")
    public R updateCountryBundleBLists(@Valid @RequestBody List<AffCountryBundleBList> bLists) {
        return R.data(service.updateCountryBundleBLists(bLists));
    }
}
