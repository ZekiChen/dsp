package com.tecdo.mapper.doris;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.entity.doris.AdGroupCost;

/**
 * 展示实时数据 Mapper
 *
 * Created by Zeki on 2023/2/21
 */
@DS("doris")
public interface AdGroupCostMapper extends BaseMapper<AdGroupCost> {
}
