package com.tecdo.adm.delivery.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.tecdo.adm.api.delivery.vo.AdsizeVO;
import com.tecdo.adm.delivery.service.IAdsizeService;
import com.tecdo.common.constant.AppConstant;
import com.tecdo.core.launch.response.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Elwin on 2023/9/13
 */
@RequestMapping(AppConstant.ADM + "/adsize")
@RestController
@Api(tags = "素材标准规格")
@RequiredArgsConstructor
public class AdsizeController {
    private final IAdsizeService service;

    @GetMapping("/standard-size")
    @ApiOperationSupport(order = 7)
    @ApiOperation(value = "获取竞价流量规格", notes = "无需传参")
    public R<List<AdsizeVO>> standardSpecs() {
        return R.data(service.standardSpecs());
    }
}
