package com.tecdo.adm.delivery.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.api.delivery.enums.CreativeTypeEnum;
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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "新增", notes = "传入素材")
    public R batchUploadFile(@RequestPart("files") MultipartFile[] files, @RequestParam Map<String, String> paramMap) {
        return service.batchUploadFiles(files, paramMap, ossTemplate);
    }

    @SneakyThrows
    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "修改", notes = "传入素材")
    public R<String> update(@RequestPart(value = "file", required = false) MultipartFile file,
                    @RequestParam("id") Integer id,
                    @RequestParam(value = "name", required = false) String name,
                    @RequestParam(value = "width", required = false) Integer width,
                    @RequestParam(value = "height", required = false) Integer height,
                    @RequestParam(value = "catIab", required = false) String catIab,
                    @RequestParam(value = "suffix", required = false) String suffix,
                    @RequestParam(value = "duration", required = false) Integer duration,
                    @RequestParam(value = "status", required = false) Integer status,
                    @RequestParam(value = "brand", required = false) String brand,
                    @RequestParam(value = "externalId", required = false) Integer externalId) {
        CacheUtil.clear(CREATIVE_CACHE);
        return service.updateCreative(ossTemplate, file, id, name, width, height, catIab, suffix, duration, status, brand, externalId);
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
    public R<IPage<CreativeVO>> page(Creative creative, PQuery query,
                                     @RequestParam(value = "ids", required = false) String ids) {
        LambdaQueryWrapper<Creative> wrapper = Wrappers.lambdaQuery();
        wrapper.in(StrUtil.isNotBlank(ids), Creative::getId, BigTool.toIntList(ids));
        wrapper.like(StrUtil.isNotBlank(creative.getName()), Creative::getName, creative.getName());
        wrapper.eq(creative.getType() != null, Creative::getType, creative.getType());
        wrapper.eq(creative.getWidth() != null, Creative::getWidth, creative.getWidth());
        wrapper.eq(creative.getHeight() != null, Creative::getHeight, creative.getHeight());
        wrapper.eq(StrUtil.isNotBlank(creative.getBrand()), Creative::getBrand, creative.getBrand());
        wrapper.eq(StrUtil.isNotBlank(creative.getUrl()), Creative::getUrl, creative.getUrl());
        wrapper.eq(StrUtil.isNotBlank(creative.getCatIab()), Creative::getCatIab, creative.getCatIab());
        wrapper.eq(creative.getStatus() != null, Creative::getStatus, creative.getStatus());
        wrapper.eq(creative.getExternalId() != null, Creative::getExternalId, creative.getExternalId());
        IPage<Creative> pages = service.page(PCondition.getPage(query), wrapper);
        IPage<CreativeVO> voPage = CreativeWrapper.build().pageVO(pages);
        List<CreativeVO> records = voPage.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            records.forEach(record -> {
                String key = record.getBrand();
                if (StrUtil.isNotBlank(key)) {
                    record.setBrandName(service.getBrandNameByKey(key));
                }
            });
        }
        voPage.setRecords(records);
        return R.data(CreativeWrapper.build().pageVO(pages));
    }

    @GetMapping("/spec-list")
    @ApiOperationSupport(order = 6)
    @ApiOperation(value = "规格列表", notes = "无需传参")
    @Cacheable(cacheNames = CREATIVE_CACHE, key = "'listSpecs'")
    public R<List<CreativeSpecVO>> listSpecs() {
        return R.data(service.listSpecs());
    }

    @SneakyThrows
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperationSupport(order = 7)
    @ApiOperation(value = "录入OBS", notes = "录入OBS")
    public R<List<PacFile>> upload(@RequestPart("files") MultipartFile[] files) {
        List<PacFile> pacFiles = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            PacFile pacFile = ossTemplate.uploadFile(files[i].getOriginalFilename(), files[i].getInputStream());
            pacFiles.add(pacFile);
        }
        return R.data(pacFiles);
    }

    @GetMapping("/generate-external-id")
    @ApiOperationSupport(order = 8)
    @ApiOperation(value = "无需传参")
    public R<Integer> generateExternalId() {
        return R.data(service.getNewExternalId());
    }
}
