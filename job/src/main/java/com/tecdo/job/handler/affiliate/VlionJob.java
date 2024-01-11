package com.tecdo.job.handler.affiliate;

import com.tecdo.adm.api.delivery.dto.SpentDTO;
import com.tecdo.job.foreign.feishu.AffReport;
import com.tecdo.job.mapper.DspReportMapper;
import com.tecdo.job.util.TimeZoneUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * UTC+8
 *
 * Created by Elwin on 2023/12/14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VlionJob {
    private final DspReportMapper reportMapper;
    private final AffReport affReport;

    @Value("${feishu.aff.vlion.cost-ratio}")
    private String costRatio;
    @Value("${feishu.aff.vlion.imp-ratio}")
    private String impRatio;
    private final String sheetId = "4abee8";
    private final String sheetToken = "E0gvsATOGhwcHptB9w5cOHfSnsf";
    private final String unitRange = "?!A3:A3";
    private final String range = "?!A3:C3";
    private final Integer affId = 134;

    @XxlJob("FeishuAff134Job")
    public void dspReport() {
        XxlJobHelper.log("获取Vlion doris库dsp_report表前一天数据，写入飞书文档");
        LocalDate today = TimeZoneUtils.dateInChina();

        String targetDate = affReport.dateFormat(today);
        SpentDTO spent = reportMapper.getImpCostForAffUTC8(targetDate, affId);

        affReport.postData(today, sheetId, sheetToken, spent, null, costRatio, impRatio, range);
        affReport.unitFormatter(sheetId, sheetToken, unitRange);
    }

}
