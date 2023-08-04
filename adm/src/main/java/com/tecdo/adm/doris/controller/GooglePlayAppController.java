package com.tecdo.adm.doris.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.tecdo.adm.api.doris.entity.GooglePlayApp;
import com.tecdo.adm.doris.IGooglePlayAppService;
import com.tecdo.common.constant.AppConstant;
import com.tecdo.core.launch.response.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by Zeki on 2023/8/3
 */
@RequestMapping(AppConstant.ADM + "/google-play-app")
@RestController
@Api(tags = "谷歌商店App")
@RequiredArgsConstructor
public class GooglePlayAppController {

    private final IGooglePlayAppService service;

    @GetMapping("/list-category")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "category列表", notes = "无需传参")
    public R<List<String>> listCategory() {
        return R.data(service.listCategory());
    }

    @GetMapping("/list-tag")
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "tag列表", notes = "无需传参")
    public R<List<String>> listTag() {
        return R.data(service.listTag());
    }

    @PostMapping("/count")
    @ApiOperationSupport(order = 3)
    @ApiOperation(value = "统计", notes = "传入categoryList和tagList")
    public R<String> count(@RequestBody GooglePlayApp entity) {
        return R.data(service.countByCategoriesAndTags(entity.getCategoryList(), entity.getTagList()));
    }
}
