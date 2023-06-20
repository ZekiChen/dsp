package com.tecdo.adm.api.doris.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.delivery.dto.ReportEventDTO;
import com.tecdo.adm.api.delivery.dto.SpentDTO;
import com.tecdo.adm.api.doris.entity.BundleData;
import com.tecdo.adm.api.doris.entity.Report;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * Created by Zeki on 2023/4/3
 */
@DS("doris-ads")
public interface ReportMapper extends BaseMapper<Report> {

  List<Report> getAeDailyReportInUsWest(@Param("dateHours") List<String> dateHours,
                                        @Param("campaignIds") Set<Integer> campaignIds);

  SpentDTO getReportSpentForFlatAds(@Param("affId") Integer affId,
                                    @Param("createDate") String createDate);

  ReportEventDTO getRepostEventForLazada(@Param("date") String date,
                                         @Param("country") String country,
                                         @Param("campaignIds") List<Integer> campaignIds);

  List<BundleData> getDataImpCountGtSize(@Param("startDate") String startDate,
                                         @Param("endDate") String endDate,
                                         @Param("size") Integer size);

  List<BundleData> getBundleData(@Param("reportHour") String reportHour);
}