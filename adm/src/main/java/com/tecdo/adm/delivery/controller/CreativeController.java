package com.tecdo.adm.delivery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.api.delivery.vo.CreativeSpecVO;
import com.tecdo.adm.api.delivery.vo.CreativeVO;
import com.tecdo.adm.delivery.service.ICreativeService;
import com.tecdo.adm.delivery.wrapper.CreativeWrapper;
import com.tecdo.common.constant.AppConstant;
import com.tecdo.core.launch.response.R;
import com.tecdo.starter.mp.support.PCondition;
import com.tecdo.starter.mp.support.PQuery;
import com.tecdo.starter.oss.OssTemplate;
import com.tecdo.starter.oss.domain.PacFile;
import com.tecdo.starter.redis.CacheUtil;
import com.tecdo.starter.tool.BigTool;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

import static com.tecdo.common.constant.CacheConstant.CREATIVE_CACHE;

/**
 * Created by Zeki on 2023/3/10
 */
@RequestMapping(AppConstant.ADM + "/creative")
@RestController
@Api(tags = "素材")
@RequiredArgsConstructor
public class CreativeController {

    private final ICreativeService service;
    private final OssTemplate ossTemplate;

    @SneakyThrows
    @PostMapping("/add")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "新增", notes = "传入素材")
    public R uploadFile(@RequestParam("file") MultipartFile file,
                        @RequestParam("name") String name,
                        @RequestParam("type") Integer type,
                        @RequestParam("width") Integer width,
                        @RequestParam("height") Integer height,
                        @RequestParam("catIab") String catIab) {
        CacheUtil.clear(CREATIVE_CACHE);
        PacFile pacFile = ossTemplate.uploadFile(file.getOriginalFilename(), file.getInputStream());
        Creative creative = new Creative();
        creative.setUrl(pacFile.getUrl());
        creative.setName(name);
        creative.setType(type);
        creative.setWidth(width);
        creative.setHeight(height);
        creative.setCatIab(catIab);
        return R.status(service.save(creative));
    }

    @PutMapping("/update")
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "修改", notes = "传入Creative")
    public R update(@Valid @RequestBody Creative creative) {
        CacheUtil.clear(CREATIVE_CACHE);
        return R.status(service.updateById(creative));
    }

    @DeleteMapping("/remove")
    @ApiOperationSupport(order = 3)
    @ApiOperation(value = "删除", notes = "传入ids")
    public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
        CacheUtil.clear(CREATIVE_CACHE);
        return R.status(service.removeByIds(BigTool.toLongList(ids)));
    }

    @GetMapping("/detail")
    @ApiOperationSupport(order = 4)
    @ApiOperation(value = "详情", notes = "传入Creative")
    public R<CreativeVO> detail(Creative creative) {
        Creative detail = service.getOne(PCondition.getQueryWrapper(creative));
        return R.data(CreativeWrapper.build().entityVO(detail));
    }

    @GetMapping("/page")
    @ApiOperationSupport(order = 5)
    @ApiOperation(value = "分页", notes = "传入Creative")
    public R<IPage<CreativeVO>> page(Creative creative, PQuery query) {
        IPage<Creative> pages = service.page(PCondition.getPage(query), PCondition.getQueryWrapper(creative));
        return R.data(CreativeWrapper.build().pageVO(pages));
    }

    @GetMapping("/spec-list")
    @ApiOperationSupport(order = 6)
    @ApiOperation(value = "规格列表", notes = "无需传参")
    @Cacheable(cacheNames = CREATIVE_CACHE, key = "'listSpecs'")
    public R<List<CreativeSpecVO>> listSpecs() {
        return R.data(service.listSpecs());
    }
}
