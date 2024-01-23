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
 * Created by Elwin on 2023/11/28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TradPlusJob {
    private final DspReportMapper reportMapper;
    private final AffReport affReport;

    @Value("${feishu.aff.trad.cost-ratio}")
    private String costRatio;
    @Value("${feishu.aff.trad.imp-ratio}")
    private String impRatio;
    private final String sheetId = "259b38";
    private final String sheetToken = "H0mvsr527hRoE5taaeMcQz0Fnug";
    private final String unitRange = "?!A3:A3";
    private final String range = "?!A3:C3";
    private final Integer affId = 140;

    @XxlJob("FeishuAff140Job")
    public void dspReport() {
        XxlJobHelper.log("获取TradPlus doris库dsp_report表前一天数据，写入飞书文档");
        LocalDate today = TimeZoneUtils.dateInChina();

        String targetDate = affReport.dateFormat(today);
        SpentDTO spent = reportMapper.getImpCostForAffUTC8(targetDate, affId);

        affReport.postData(today, sheetId, sheetToken, spent, null, costRatio, impRatio, range);
        affReport.unitFormatter(sheetId, sheetToken, unitRange);
    }
}
