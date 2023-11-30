package com.tecdo.job.handler.affiliate;

import com.tecdo.adm.api.delivery.dto.SpentDTO;
import com.tecdo.job.foreign.feishu.AffReport;
import com.tecdo.job.mapper.DspReportMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by Elwin on 2023/9/18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CamScannerJob {

    @Value("${feishu.aff.cs.cost-ratio}")
    private String costRatio;
    @Value("${feishu.aff.cs.imp-ratio}")
    private String impRatio;
    @Value("${feishu.aff.cs.sheet-id}")
    private String sheetId;
    @Value("${feishu.aff.cs.sheet-token}")
    private String sheetToken;

    private final DspReportMapper reportMapper;
    private final AffReport affReport;

    private final Integer affId = 127;

    @XxlJob("FeishuAff127Job")
    public void dspReport() {
        XxlJobHelper.log("获取Camscanner doris库dsp_report表前一天数据，写入飞书文档");
        SpentDTO spent = reportMapper.getImpCostForAff(affReport.dateFormat(), affId);

        affReport.postData(sheetId, sheetToken, spent, costRatio, impRatio);
        affReport.unitFormatter(sheetId, sheetToken);
    }


}
