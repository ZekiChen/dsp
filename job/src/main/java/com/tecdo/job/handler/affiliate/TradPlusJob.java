package com.tecdo.job.handler.affiliate;

import com.tecdo.adm.api.delivery.dto.SpentDTO;
import com.tecdo.job.foreign.feishu.AffReport;
import com.tecdo.job.mapper.DspReportMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by Elwin on 2023/11/28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TradPlusJob {
    @Value("${feishu.aff.trad.cost-ratio}")
    private String costRatio;
    @Value("${feishu.aff.trad.imp-ratio}")
    private String impRatio;
    @Value("${feishu.aff.trad.sheet-id}")
    private String sheetId;
    @Value("${feishu.aff.trad.sheet-token}")
    private String sheetToken;

    private final DspReportMapper reportMapper;
    private final AffReport affReport;

    private final Integer affId = 140;

    @XxlJob("FeishuAff140Job")
    public void dspReport() {
        XxlJobHelper.log("获取TradPlus doris库dsp_report表前一天数据，写入飞书文档");
        SpentDTO spent = reportMapper.getImpCostForAff(affReport.dateFormat(), affId);

        affReport.postData(sheetId, sheetToken, spent, costRatio, impRatio);
        affReport.unitFormatter(sheetId, sheetToken);
    }
}
