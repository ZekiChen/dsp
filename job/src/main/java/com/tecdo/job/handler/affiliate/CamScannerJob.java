package com.tecdo.job.handler.affiliate;

import com.tecdo.adm.api.delivery.dto.SpentDTO;
import com.tecdo.job.constant.ReportConstant;
import com.tecdo.job.foreign.feishu.AffReport;
import com.tecdo.job.mapper.DspReportMapper;
import com.tecdo.job.util.TimeZoneUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;

/**
 * Created by Elwin on 2023/9/18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CamScannerJob {
    private final DspReportMapper reportMapper;
    private final AffReport affReport;

    @Value("${feishu.aff.cs.cost-ratio}")
    private String costRatio;
    @Value("${feishu.aff.cs.imp-ratio}")
    private String impRatio;
    private final String sheetId = "0102f8";
    private final String sheetToken = "QFI6s3KWCh52AdtiLadciQXmnHZ";
    private final String unitRange = "?!A2:A2";
    private final String range = "?!A2:C2";
    private final Integer affId = 127;

    @XxlJob("FeishuAff127Job")
    public void dspReport() {
        XxlJobHelper.log("获取Camscanner doris库dsp_report表前一天数据，写入飞书文档");
        LocalDate today = TimeZoneUtils.dateInChina();

        String targetDate = affReport.dateFormat(today);
        SpentDTO spent = reportMapper.getImpCostForAffUTC8(targetDate, affId);

        affReport.postData(today, sheetId, sheetToken, spent, null, costRatio, impRatio, range);
        affReport.unitFormatter(sheetId, sheetToken, unitRange);
    }


}
