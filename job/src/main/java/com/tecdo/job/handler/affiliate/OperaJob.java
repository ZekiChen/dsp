package com.tecdo.job.handler.affiliate;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.URLUtil;
import com.ejlchina.data.Mapper;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.OkHttps;
import com.mysql.cj.util.TimeUtil;
import com.tecdo.adm.api.delivery.dto.SpentDTO;
import com.tecdo.job.foreign.feishu.AffReport;
import com.tecdo.job.mapper.DspReportMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * UTC+0
 *
 * Created by Elwin on 2024/1/10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperaJob {
    private final DspReportMapper reportMapper;
    private final AffReport affReport;

    @Value("${feishu.aff.opera.cost-ratio}")
    private String costRatio;
    @Value("${feishu.aff.opera.imp-ratio}")
    private String impRatio;
    private final String sheetId = "a8a102";
    private final String sheetToken = "XWx5sRXFOhngsLtZuhAcSzAfnGg";
    private final String unitRange = "?!A3:A3";
    private final String range = "?!A3:G3";
    private final Integer affId = 141;
    private final String operaApiUrl = "https://ofa.adx.opera.com/oapi/report/dsp_v2";
    private final String operaApiToken = "jwxLZuC8PnjM4uJfA4TNAqcImozOVtzo";

    @XxlJob("FeishuAff141Job")
    public void dspReport() {
        XxlJobHelper.log("获取Opera doris库dsp_report表前一天数据(UTC-0)，写入飞书文档");
        LocalDate today = LocalDate.now();

        String targetDate = affReport.dateFormat(today);
        SpentDTO dspSpent = reportMapper.getImpCostForAffUTC0(targetDate, affId);
        SpentDTO affSpent = getOperaSpent(targetDate);

        affReport.postData(today, sheetId, sheetToken, dspSpent, affSpent, costRatio, impRatio, range);
        affReport.unitFormatter(sheetId, sheetToken, unitRange);
        affReport.gapMsgWarn(dspSpent, affSpent, 141, "Opera", targetDate);

        affReport.insertGapReport(affId, targetDate, dspSpent, affSpent);
    }

    /**
     * 获取opera的SpentDTO对象
     * @param date 日期
     * @return SpentDTO对象
     */
    private SpentDTO getOperaSpent(String date) {
        String url =  UrlBuilder.of(operaApiUrl)
                .addQuery("token", operaApiToken)
                .addQuery("start_date", date)
                .addQuery("end_date", date)
                .toString();

        Mapper result = OkHttps.sync(url)
                .bodyType(OkHttps.JSON)
                .get()
                .getBody()
                .toMapper();

        // NOTE: 若getLong() / getDouble()对象不存在，则会返回0L/0D,因此不做判空处理
        Mapper data = result.getArray("data").getMapper(0);
        SpentDTO affSpent = new SpentDTO();
        affSpent.setImp(data.getLong("impressionCount"));
        affSpent.setCost(data.getDouble("spent"));

        return affSpent;
    }

}
