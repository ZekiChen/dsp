package com.tecdo.adm.api.doris.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.adm.api.delivery.dto.SpentDTO;
import com.tecdo.adm.api.doris.dto.AutoBundle;
import com.tecdo.adm.api.doris.dto.BundleCost;
import com.tecdo.adm.api.doris.dto.ECPX;
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

  List<BundleData> getDataImpCountGtSize(@Param("startDate") String startDate,
                                         @Param("endDate") String endDate,
                                         @Param("size") Integer size);

  List<BundleData> getBundleData(@Param("reportHour") String reportHour);

  List<ECPX> listECPX(@Param("startDate") String startDate, @Param("endDate") String endDate);

  List<BundleCost> getBundleCostByDay(@Param("createDate") String createDate);

  /**
   * 获取当日指定adGroup列表内的bundle
   * @param adGroups 指定的adGroup列表
   * @param createDate 创建日期
   * @return bundle坐标（ad_group_id-bundle_id）
   */
  List<AutoBundle> getAutoBundleInfoList(@Param("adGroups") List<Integer> adGroups,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate);

  /**
   * 获取AutoBundle的click, imp, ctr信息
   * @param bundles AutoBundle列表
   * @param startDate 前数第五天
   * @param endDate 前数第一天
   * @return AutoBundle列表
   */
  List<AutoBundle> getAutoBundleInfo(@Param("bundles") List<AutoBundle> bundles,
                                     @Param("startDate") String startDate,
                                     @Param("endDate") String endDate);
}
