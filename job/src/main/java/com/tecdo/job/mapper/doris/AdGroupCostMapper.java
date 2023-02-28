package com.tecdo.job.mapper.doris;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.job.entity.doris.AdGroupCost;

/**
 * 展示实时数据 Mapper
 *
 * Created by Zeki on 2023/2/21
 */
@DS("doris")
public interface AdGroupCostMapper extends BaseMapper<AdGroupCost> {
}
