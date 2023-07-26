package com.tecdo.mapper.doris;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.entity.CheatingData;
import com.tecdo.entity.CheatingDataSize;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@DS("doris-ads")
public interface CheatingDataMapper extends BaseMapper<CheatingData> {

  @Select(
    "SELECT distinct hash_code as hashCode,cheat_key as cheatKey FROM `cheating` WHERE hash_code > #{hashCode} " +
    "and end_time > #{endTime} and reason = #{reason} order by hash_code limit #{batchSize}")
  List<CheatingData> getCheatingData(@Param("hashCode") Long hashCode,
                                     @Param("endTime") String endTime,
                                     @Param("reason") String reason,
                                     @Param("batchSize") Integer batchSize);

  @Select("SELECT reason, COUNT(DISTINCT cheat_key) as dataSize FROM `cheating` WHERE end_time > #{endTime} GROUP BY reason")
  List<CheatingDataSize> selectSize(@Param("endTime") String endTime);

}
