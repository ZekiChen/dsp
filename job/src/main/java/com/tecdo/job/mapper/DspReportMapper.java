package com.tecdo.job.mapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tecdo.adm.api.delivery.dto.SpentDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


/**
 * 渠道报表 Mapper 接口
 * Created by Elwin on 2023/9/19
 */
@DS("doris-ads")
public interface DspReportMapper{
    @Select("SELECT SUM(imp_count) as imp, ROUND(SUM(imp_success_price_total) / 1000, 4) as cost " +
            "FROM dsp_report " +
            "WHERE Date(CONVERT_TZ(STR_TO_DATE(create_time, '%Y-%m-%d_%H'), '+00:00', '+08:00')) = #{date} " +
            "and affiliate_id = #{affId}")
    SpentDTO getImpCostForAff(@Param("date") String date, @Param("affId") Integer affId);
}
