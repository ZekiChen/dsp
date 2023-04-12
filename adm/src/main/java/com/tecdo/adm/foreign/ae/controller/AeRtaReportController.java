package com.tecdo.adm.foreign.ae.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.tecdo.adm.api.foreign.ae.vo.request.AeDailyCostVO;
import com.tecdo.adm.api.foreign.ae.vo.response.AeDataVO;
import com.tecdo.adm.api.foreign.ae.vo.response.AeReportVO;
import com.tecdo.adm.api.foreign.ae.vo.response.AeResponse;
import com.tecdo.adm.foreign.ae.service.IAeReportService;
import com.tecdo.common.constant.AppConstant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Created by Zeki on 2023/4/4
 */
@RequestMapping(AppConstant.ADM + "/ae/rta")
@RestController
@Api(tags = "供AE侧调用的RTA报表接口")
@RequiredArgsConstructor
public class AeRtaReportController {

    private final IAeReportService aeReportService;

    @PostMapping("/daily/report")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "查看某天报表", notes = "传入AeDailyCostVO")
    public AeResponse<AeDataVO<AeReportVO>> dailyReport(@Valid @RequestBody AeDailyCostVO vo) {
        return AeResponse.data(aeReportService.listAdvCampaignDailyReport(vo));
    }
}
