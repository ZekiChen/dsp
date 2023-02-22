package com.tecdo.mapper.doris;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.entity.doris.AdGroupImpCount;

/**
 * 展示实时数据 Mapper
 * <p>
 * Created by Zeki on 2023/2/21
 */
@DS("doris")
public interface AdGroupImpCountMapper extends BaseMapper<AdGroupImpCount> {
}
