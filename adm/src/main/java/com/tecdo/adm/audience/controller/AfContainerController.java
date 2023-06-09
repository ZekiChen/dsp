package com.tecdo.adm.audience.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.tecdo.adm.api.audience.vo.SimpleAfContainerVO;
import com.tecdo.adm.audience.service.IAfContainerService;
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
 * Created by Zeki on 2023/6/8
 */
@RequestMapping(AppConstant.ADM + "/af-container")
@RestController
@Api(tags = "AF人群包")
@RequiredArgsConstructor
public class AfContainerController {

    private final IAfContainerService service;

    @GetMapping("/list")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "列表", notes = "无需传参")
    public R<List<SimpleAfContainerVO>> listSimple() {
        return R.data(service.listSimple());
    }
}
