package com.tecdo.adm.doris.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.doris.entity.GooglePlayApp;
import com.tecdo.adm.api.doris.mapper.GooglePlayAppMapper;
import com.tecdo.adm.doris.IGooglePlayAppService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/8/3
 */
@Service
public class GooglePlayAppServiceImpl extends ServiceImpl<GooglePlayAppMapper, GooglePlayApp> implements IGooglePlayAppService {

    @Override
    public List<String> listCategory() {
        List<String> categories = baseMapper.listCategory();
        return categories.stream()
                .flatMap(c -> Arrays.stream(c.split(StrUtil.COMMA)))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listTag() {
        List<String> tags = baseMapper.listTag();
        return tags.stream()
                .flatMap(c -> Arrays.stream(c.split(StrUtil.COMMA)))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public String countByCategoriesAndTags(List<String> categoryList, List<String> tagList) {
        return baseMapper.countByCategoriesAndTags(categoryList, tagList);
    }

    @Override
    public List<String> listByCategoriesAndTags(List<String> categoryList, List<String> tagList) {
        return baseMapper.listByCategoriesAndTags(categoryList, tagList);
    }
}
