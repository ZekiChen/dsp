package com.tecdo.job.handler.affiliate;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.OkHttps;
import com.tecdo.adm.api.delivery.dto.SpentDTO;
import com.tecdo.adm.api.doris.mapper.ReportMapper;
import com.tecdo.job.domain.entity.ReportAffGap;
import com.tecdo.job.domain.vo.flatads.FlatAdsReportVO;
import com.tecdo.job.domain.vo.flatads.FlatAdsResponse;
import com.tecdo.job.foreign.feishu.AffReport;
import com.tecdo.job.mapper.ReportAffGapMapper;
import com.tecdo.common.util.WeChatRobotUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.tecdo.job.util.MonitorGroupHelper.MONITOR_GROUP;
import static com.tecdo.job.util.MonitorGroupHelper.logError;

/**
 * Created by Zeki on 2023/5/29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlatAdsJob {

    @Value("${foreign.flat-ads.report-url}")
    private String flatAdsReportUrl;
    @Value("${pac.dsp.aff.gap}")
    private Double gap;

    private final ReportMapper reportMapper;
    private final ReportAffGapMapper reportAffGapMapper;
    private final AffReport affReport;

    @Value("${feishu.aff.flatads.cost-ratio}")
    private String costRatio;
    @Value("${feishu.aff.flatads.imp-ratio}")
    private String impRatio;
    private final String sheetId = "a8a102";
    private final String sheetToken = "HeBqs614FhkS0NtOWb0cCjvjntc";
    private final String unitRange = "?!A3:A3";
    private final String range = "?!A3:G3";

    @XxlJob("FeishuAff93&130Job")
    public void dspReport() {
        XxlJobHelper.log("获取FlatAds doris库dsp_report表前一天数据(UTC-0)，写入飞书文档");
        LocalDate today = LocalDate.now();

        String targetDate = affReport.dateFormat(today);
        SpentDTO dspSpent = doGetReportSpentForFlatAds();
        SpentDTO affSpent = getFlatAdsSpent(targetDate.replace("-", ""));
        if (affSpent == null) return;

        affReport.postData(today, sheetId, sheetToken, dspSpent, affSpent, costRatio, impRatio, range);
        affReport.unitFormatter(sheetId, sheetToken, unitRange);

        affReport.insertGapReport(93, targetDate, dspSpent, affSpent);
    }

    private static ReportAffGap buildDspAffGap(Integer affiliateId, String createDate,
                                               Long flatAdsImp, String flatAdsCostStr,
                                               Long dspImp, String dspCostStr,
                                               String impGapStr, String costGapStr) {
        ReportAffGap entity = new ReportAffGap();
        entity.setCreateDate(createDate);
        entity.setAffId(affiliateId);
        entity.setAffImp(flatAdsImp);
        entity.setDspImp(dspImp);
        entity.setGapImp(Double.valueOf(impGapStr));
        entity.setAffCost(Double.valueOf(flatAdsCostStr));
        entity.setDspCost(Double.valueOf(dspCostStr));
        entity.setGapCost(Double.valueOf(costGapStr));
        return entity;
    }

    private SpentDTO doGetReportSpentForFlatAds() {
        List<Integer> affIds = Arrays.asList(93, 130);
        return reportMapper.getReportSpentForFlatAds(affIds, DateUtil.yesterday().toDateStr());
    }

    private SpentDTO getFlatAdsSpent(String date) {
        Map<String, String> conditionMap = MapUtil.newHashMap();
        conditionMap.put("dt_from", date);
        conditionMap.put("dt_to", date);
        Map<String, Object> paramMap = MapUtil.newHashMap();
        paramMap.put("condition", conditionMap);
        HttpResult result = OkHttps.sync(flatAdsReportUrl).bodyType(OkHttps.JSON).addBodyPara(paramMap).post();

        if (result.isSuccessful()) {
            FlatAdsResponse response = result.getBody().toBean(FlatAdsResponse.class);
            List<FlatAdsReportVO> reportVOs = response.getData();
            if (CollUtil.isEmpty(reportVOs)) {
                XxlJobHelper.log("call FlatAds report error, data is empty, date: {}" + date);
                return null;
            }
            FlatAdsReportVO reportVO = reportVOs.get(0);
            Long flatAdsImp = reportVO.getImpression();
            Double flatAdsCost = reportVO.getGrossRevenue();
            if (flatAdsCost == null || flatAdsImp == null) {
                XxlJobHelper.log("call FlatAds report error, data is empty, date: {}" + date);
                return null;
            }

            SpentDTO flatadsDTO = new SpentDTO();
            flatadsDTO.setCost(flatAdsCost);
            flatadsDTO.setImp(flatAdsImp);
            return flatadsDTO;
        }
        return null;
    }

}
