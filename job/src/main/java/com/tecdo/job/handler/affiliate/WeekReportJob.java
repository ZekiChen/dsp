package com.tecdo.job.handler.affiliate;

import cn.hutool.core.date.DateUtil;
import com.ejlchina.data.Array;
import com.ejlchina.data.Mapper;
import com.tecdo.adm.api.doris.dto.AffWeekReport;
import com.tecdo.adm.api.doris.mapper.ReportMapper;
import com.tecdo.job.foreign.feishu.AffReport;
import com.tecdo.job.util.TimeZoneUtils;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Elwin on 2023/12/20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeekReportJob {
    private final AffReport affReport;
    private final ReportMapper reportMapper;

    // 渠道表token数组
    @Value("${feishu.aff.week-report.sheetTokens}")
    private String sheetTokensStr;

    @XxlJob("affWeekReportJob")
    public void dspReport() {
        String[] sheetTokens = sheetTokensStr.split(",");
        String tenantToken = affReport.getAccessToken();

        // 按渠道表更新
        for (String sheetToken : sheetTokens) {
            Mapper metaInfo = affReport.getMetaInfo(sheetToken, tenantToken); // 获取渠道表的meta信息
            int affId = Integer.parseInt(metaInfo.getMapper("data").getMapper("properties").getString("title"));

            // 每张渠道表下有自己的国家子表
            Array sheetsInfo = metaInfo.getMapper("data").getArray("sheets"); // 包含国家代码+sheetId

            // 1张汇总表 & n-1张国家表
            String[] countries = getCountries(sheetsInfo);
            String[] sheetIds = getSheetIds(sheetsInfo);

            // 遍历国家表
            for (int j = 0; j < sheetsInfo.size(); j++) {
                String sheetId = sheetIds[j];
                String country = countries[j];
                String startDate = DateUtil.format(TimeZoneUtils.dateTimeInChina().minusDays(8), "yyyy-MM-dd_16");
                String endDate = DateUtil.format(TimeZoneUtils.dateTimeInChina().minusDays(1), "yyyy-MM-dd_16");

                // 获取周报数据
                List<AffWeekReport> reportVOList = new ArrayList<>();
                if (j == 0) {
                    reportVOList = reportMapper
                            .getAffWeekReport(startDate, endDate, affId, countries);
                } else {
                    reportVOList = reportMapper
                            .getAffWeekReportByCountry(startDate, endDate, affId, country);
                }
                reportVOList = reportVOList.stream().map(v -> {
                    String modifiedDate = v.getDate().replace("-", "/");
                    v.setDate(modifiedDate);
                    return v;
                }).collect(Collectors.toList());

                // 写入飞书表格
                affReport.postWeekData(sheetId, sheetToken, reportVOList, tenantToken);
            }

        }
    }

    public String[] getCountries(Array sheetsInfo) {
        String[] countries = new String[sheetsInfo.size()];
        for (int i = 0; i < countries.length; i++) {
            countries[i] = sheetsInfo.getMapper(i).getString("title");
        }
        return countries;
    }

    public String[] getSheetIds(Array sheetsInfo) {
        String[] sheetIds = new String[sheetsInfo.size()];
        for (int i = 0; i < sheetIds.length; i++) {
            sheetIds[i] = sheetsInfo.getMapper(i).getString("sheetId");
        }
        return sheetIds;
    }
}
