package com.tecdo.adm.log.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.tecdo.adm.api.log.entity.BizLogApi;
import com.tecdo.adm.api.log.vo.BizLogApiVO;
import com.tecdo.adm.delivery.wrapper.BizLogApiWrapper;
import com.tecdo.adm.log.service.IBizLogApiService;
import com.tecdo.common.constant.AppConstant;
import com.tecdo.core.launch.response.R;
import com.tecdo.starter.mp.support.PCondition;
import com.tecdo.starter.mp.support.PQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Zeki on 2023/4/5
 */
@RequestMapping(AppConstant.ADM + "/biz-log-api")
@RestController
@Api(tags = "业务接口日志")
@RequiredArgsConstructor
public class BizLogApiController {

    private final IBizLogApiService service;

    @GetMapping("/page")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "分页", notes = "传入BizLogApi")
    public R<IPage<BizLogApiVO>> page(BizLogApi entity, PQuery query,
                                      @RequestParam(value = "startTime", required = false) String startTime,
                                      @RequestParam(value = "endTime", required = false) String endTime) {
        LambdaQueryWrapper<BizLogApi> wrapper = PCondition.getQueryWrapper(entity).lambda();
        if (StrUtil.isAllNotBlank(startTime, endTime)) {
            wrapper.between(BizLogApi::getCreateTime, startTime, endTime);
        }
        IPage<BizLogApi> pages = service.page(PCondition.getPage(query), wrapper);
        return R.data(BizLogApiWrapper.build().pageVO(pages));
    }
}
