package com.tecdo.adm.api.doris.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.doris.entity.AdsDi;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * Created by Zeki on 2023/4/3
 */
@DS("doris-ads")
public interface AdsDiMapper extends BaseMapper<AdsDi> {

    List<AdsDi> getAeDailyReportInUsWest(@Param("yesterday") String yesterday,
                                          @Param("today") String today,
                                          @Param("campaignIds") Set<Integer> campaignIds);
}
