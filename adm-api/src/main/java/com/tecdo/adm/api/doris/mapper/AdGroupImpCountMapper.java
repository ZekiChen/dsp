package com.tecdo.adm.api.doris.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.doris.entity.AdGroupImpCount;

/**
 * 展示实时数据 Mapper
 * <p>
 * Created by Zeki on 2023/2/21
 */
@DS("doris")
public interface AdGroupImpCountMapper extends BaseMapper<AdGroupImpCount> {
}
