package com.tecdo.adm.api.delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.delivery.entity.MultiBidStrategy;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 多阶段策略 Mapper接口
 *
 * Created by Elwin on 2023/11/3
 */
public interface MultiBidStrategyMapper extends BaseMapper<MultiBidStrategy> {
    void insertOrUpdate(@Param("strategies") List<MultiBidStrategy> strategies);
}
