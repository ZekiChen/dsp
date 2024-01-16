package com.tecdo.job.handler.affiliate;

import cn.hutool.core.net.url.UrlBuilder;
import com.ejlchina.data.Mapper;
import com.ejlchina.okhttps.OkHttps;
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
    private final String range = "?!A3:G3";
    private final Integer affId = 134;
    private final String vlionApiUrl = "http://report.vlion.cn/adx_dsp/Tecdo_DSP/8b5f6e54c5c3647f2deed77ee10f867c";

    @XxlJob("FeishuAff134Job")
    public void dspReport() {
        XxlJobHelper.log("获取Vlion doris库dsp_report表前一天数据，写入飞书文档");
        LocalDate today = LocalDate.now();

        String targetDate = affReport.dateFormat(today);
        SpentDTO dspSpent = reportMapper.getImpCostForAffUTC0(targetDate, affId);
        SpentDTO affSpent = getVlionSpent(targetDate);

        affReport.postData(today, sheetId, sheetToken, dspSpent, affSpent, costRatio, impRatio, range);
        affReport.unitFormatter(sheetId, sheetToken, unitRange);

        affReport.insertGapReport(affId, targetDate, dspSpent, affSpent);
    }

    private SpentDTO getVlionSpent(String date) {
        String url =  UrlBuilder.of(vlionApiUrl)
                .addQuery("start_date", date)
                .addQuery("end_date", date)
                .toString();
        Mapper result = OkHttps.sync(url)
                .bodyType(OkHttps.JSON)
                .get()
                .getBody()
                .toMapper();
        Mapper data = result.getArray("data").getMapper(0);
        SpentDTO affSpent = new SpentDTO();
        affSpent.setImp(data.getLong("imp"));
        affSpent.setCost(data.getDouble("revenue"));

        return affSpent;
    }

}
