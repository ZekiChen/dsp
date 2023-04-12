package com.tecdo.adm.api.doris.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.doris.entity.AdGroupClick;

/**
 * Created by Zeki on 2023/4/3
 */
@DS("doris")
public interface AdGroupClickMapper extends BaseMapper<AdGroupClick> {
}
