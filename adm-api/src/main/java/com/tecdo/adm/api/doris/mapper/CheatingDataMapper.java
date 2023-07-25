package com.tecdo.adm.api.doris.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.doris.entity.CheatingData;
import com.tecdo.adm.api.doris.entity.CheatingDataSize;

import org.apache.ibatis.annotations.Param;

import java.util.List;

@DS("doris-ads")
public interface CheatingDataMapper extends BaseMapper<CheatingData> {

  List<CheatingData> getCheatingData(@Param("hashCode") Long hashCode,
                                     @Param("endTime") String endTime,
                                     @Param("reason") String reason,
                                     @Param("batchSize") Integer batchSize);

  List<CheatingDataSize> selectSize(@Param("endTime") String endTime);

}
