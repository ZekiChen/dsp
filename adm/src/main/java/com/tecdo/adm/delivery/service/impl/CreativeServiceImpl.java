package com.tecdo.adm.delivery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.api.delivery.enums.CreativeTypeEnum;
import com.tecdo.adm.api.delivery.mapper.CreativeMapper;
import com.tecdo.adm.api.delivery.vo.AdGroupVO;
import com.tecdo.adm.api.delivery.vo.CreativeSpecVO;
import com.tecdo.adm.delivery.service.ICreativeService;
import com.tecdo.adm.log.service.IBizLogApiService;
import com.tecdo.adm.system.service.IDictService;
import com.tecdo.core.launch.response.R;
import com.tecdo.starter.oss.OssTemplate;
import com.tecdo.starter.oss.domain.PacFile;
import com.tecdo.starter.redis.CacheUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.tecdo.common.constant.CacheConstant.CREATIVE_CACHE;

/**
 * Created by Zeki on 2023/3/10
 */
@Service
@RequiredArgsConstructor
public class CreativeServiceImpl extends ServiceImpl<CreativeMapper, Creative> implements ICreativeService {

    private final IDictService dictService;
    private final IBizLogApiService bizLogApiService;

    private final int maxAttemptCnt = 3;

    @Override
    public List<CreativeSpecVO> listSpecs() {
        return baseMapper.listSpecs();
    }

    @Override
    public List<Integer> listIdByLikeName(String name) {
        return baseMapper.listIdByLikeName(name);
    }

    @Override
    public List<Integer> listIdBySize(Integer width, Integer height) {
        return baseMapper.listIdBySize(width, height);
    }

    @Override
    public String getBrandNameByKey(String key) {
        return dictService.getValue("creative_brand", key);
    }

    @Override
    public Integer getNewExternalId() {
        return baseMapper.getMaxExteranlId() + 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R batchUploadFiles(MultipartFile[] files, Map<String, String> paramMap, OssTemplate ossTemplate) throws IOException {
        int attempCnt = 0;

        while (attempCnt < maxAttemptCnt) {
            CacheUtil.clear(CREATIVE_CACHE);
            List<Creative> entities = new ArrayList<>();
            Integer newExternalId = getNewExternalId();
            for (int i = 0; i < files.length; i++) {
                PacFile pacFile = ossTemplate.uploadFile(files[i].getOriginalFilename(), files[i].getInputStream());
                Creative creative = new Creative();
                creative.setUrl(pacFile.getUrl());
                creative.setName(paramMap.get("name" + i));
                creative.setType(Integer.parseInt(paramMap.get("type" + i)));
                creative.setWidth(Integer.parseInt(paramMap.get("width" + i)));
                creative.setHeight(Integer.parseInt(paramMap.get("height" + i)));
                creative.setExternalId(newExternalId + i);
                String catIab = paramMap.get("catIab" + i);
                if (StrUtil.isNotBlank(catIab)) {
                    creative.setCatIab(catIab);
                }
                creative.setSuffix(paramMap.get("suffix" + i));
                String brand = paramMap.get("brand" + i);
                if (StrUtil.isNotBlank(brand)) {
                    creative.setBrand(brand);
                }
                if (CreativeTypeEnum.VIDEO.getType() == creative.getType()) {
                    creative.setDuration(Integer.parseInt(paramMap.get("duration" + i)));
                }
                entities.add(creative);
            }

            try {
                return R.status(saveBatch(entities));
            } catch (DuplicateKeyException e) {
                // 处理external_id duplicate 异常，可以在这里记录日志或执行其他操作
                System.out.println("Duplicate external_id. Retrying...");
                // 手动回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                attempCnt++;
            }
        }

        // 多个用户同时操作，导致生成的external_id不断冲突
        return R.failure("网络拥堵，请稍后重试！");
    }

    @Override
    public R<String> updateCreative(OssTemplate ossTemplate, MultipartFile file, Integer id, String name,
                            Integer width, Integer height, String catIab,
                            String suffix, Integer duration, Integer status,
                            String brand, Integer externalId) throws IOException {
        Creative entity = getById(id);
        if (entity == null) {
            return R.failure();
        }
        Creative before = Objects.requireNonNull(BeanUtil.copyProperties(entity, Creative.class));
        if (file != null) {
            PacFile pacFile = ossTemplate.uploadFile(file.getOriginalFilename(), file.getInputStream());
            entity.setUrl(pacFile.getUrl());
        }
        entity.setName(name);
        entity.setWidth(width);
        entity.setHeight(height);
        entity.setCatIab(StrUtil.isNotBlank(catIab) ? catIab : null);
        entity.setSuffix(suffix);
        entity.setBrand(StrUtil.isNotBlank(brand) ? brand : null);
        if (CreativeTypeEnum.VIDEO.getType() == entity.getType()) {
            entity.setDuration(duration);
        }
        entity.setStatus(status);
        entity.setExternalId(externalId);

        try {
            boolean result = updateById(entity);
            bizLogApiService.logByUpdateCreative(before, entity);
            return R.status(result);
        } catch (DuplicateKeyException e) {
            // 多个用户同时操作，导致生成的external_id冲突
            return R.data("external id被占用，请重新update!");
        }
    }
}
