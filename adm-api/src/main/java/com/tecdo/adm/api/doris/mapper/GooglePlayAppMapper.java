package com.tecdo.adm.api.doris.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.doris.entity.GooglePlayApp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@DS("doris-ads")
public interface GooglePlayAppMapper extends BaseMapper<GooglePlayApp> {

    List<String> listCategory();

    List<String> listTag();

    String countByCategoriesAndTags(@Param("categoryList") List<String> categoryList,
                                    @Param("tagList") List<String> tagList);
}
