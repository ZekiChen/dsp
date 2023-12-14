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
 * Created by Elwin on 2023/12/14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VlionJob {
    @Value("${feishu.aff.vlion.cost-ratio}")
    private String costRatio;
    @Value("${feishu.aff.vlion.imp-ratio}")
    private String impRatio;
    @Value("${feishu.aff.vlion.sheet-id}")
    private String sheetId;
    @Value("${feishu.aff.vlion.sheet-token}")
    private String sheetToken;
    @Value("${feishu.aff.vlion.sheet-unit-range}")
    private String unitRange;
    @Value("${feishu.aff.vlion.sheet-range}")
    private String range;

    private final DspReportMapper reportMapper;
    private final AffReport affReport;

    private final Integer affId = 134;

    @XxlJob("FeishuAff134Job")
    public void dspReport() {
        XxlJobHelper.log("获取Vlion doris库dsp_report表前一天数据，写入飞书文档");
        LocalDate today = TimeZoneUtils.dateInChina();

        String targetDate = affReport.dateFormat(today);
        SpentDTO spent = reportMapper.getImpCostForAffUTC8(targetDate, affId);

        affReport.postData(today, sheetId, sheetToken, spent, costRatio, impRatio, range);
        affReport.unitFormatter(sheetId, sheetToken, unitRange);
    }

}
