package com.tecdo.adm.api.doris.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.doris.entity.CheatingData;

import org.apache.ibatis.annotations.Param;

import java.util.List;

@DS("doris-ads")
public interface CheatingDataMapper extends BaseMapper<CheatingData> {

  List<CheatingData> getCheatingData(@Param("hashCode") Integer hashCode,
                                     @Param("batchSize") Integer batchSize);

}
