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
import com.tecdo.job.mapper.ReportAffGapMapper;
import com.tecdo.common.util.WeChatRobotUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    @XxlJob("flatAdsGap")
    public void flatAdsGap() {
        XxlJobHelper.log("获取前一天数据，落库FlatAds和报表gap，超过阈值则告警");
        DateTime yesterday = DateUtil.yesterday();
        String stdYesterday = yesterday.toDateStr();
        String yesterdayStr = DateUtil.format(yesterday, DatePattern.PURE_DATE_PATTERN);
        Map<String, String> conditionMap = MapUtil.newHashMap();
        conditionMap.put("dt_from", yesterdayStr);
        conditionMap.put("dt_to", yesterdayStr);
        Map<String, Object> paramMap = MapUtil.newHashMap();
        paramMap.put("condition", conditionMap);
        HttpResult result = OkHttps.sync(flatAdsReportUrl).bodyType(OkHttps.JSON).addBodyPara(paramMap).post();
        if (result.isSuccessful()) {
            FlatAdsResponse response = result.getBody().toBean(FlatAdsResponse.class);
            List<FlatAdsReportVO> reportVOs = response.getData();
            if (CollUtil.isEmpty(reportVOs)) {
                XxlJobHelper.log("call FlatAds report error, data is empty, date: {}" + stdYesterday);
                return;
            }
            FlatAdsReportVO reportVO = reportVOs.get(0);
            Long flatAdsImp = reportVO.getImpression();
            Double flatAdsCost = reportVO.getGrossRevenue();
            if (flatAdsCost == null || flatAdsImp == null) {
                XxlJobHelper.log("call FlatAds report error, data is empty, date: {}" + stdYesterday);
                return;
            }
            SpentDTO dspSpent = doGetReportSpentForFlatAds();
            if (dspSpent == null) {
                XxlJobHelper.log("get report spent for flatAds is null, date: " + stdYesterday);
                return;
            }
            long dspImp = dspSpent.getImp() != null ? dspSpent.getImp() : 0L;
            double dspCost = dspSpent.getCost() != null ? dspSpent.getCost() : 0d;
            if (dspImp == 0L || dspCost == 0d) {
                XxlJobHelper.log("get report imp/cost for flatAds is 0, date: " + stdYesterday);
                return;
            }
            double impGap = Math.abs((double) (dspImp - flatAdsImp) / dspImp) * 100;
            double costGap = Math.abs((dspCost - flatAdsCost) / dspCost) * 100;
            // 群消息通知时，保留2位小数
            String flatAdsCostStr = NumberUtil.round(flatAdsCost, 2).toString();
            String dspCostStr = NumberUtil.round(dspCost, 2).toString();
            String impGapStr = NumberUtil.round(impGap, 2).toString();
            String costGapStr = NumberUtil.round(costGap, 2).toString();
            if (costGap > gap) {
                String msg = "报表与渠道gap差异监控\n"
                        + "渠道：FlatAds\n"
                        + "日期：" + DateUtil.yesterday().toDateStr() + "\n"
                        + "渠道imp：" + flatAdsImp + "\n"
                        + "报表imp：" + dspImp + "\n"
                        + "imp gap：" + impGapStr + "%\n"
                        + "渠道cost：" + flatAdsCostStr + "$\n"
                        + "报表cost：" + dspCostStr + "$\n"
                        + "cost gap：" + costGapStr + "%";
                try {
                    WeChatRobotUtils.sendTextMsg(MONITOR_GROUP, msg);
                } catch (Exception e) {
                    logError("flatAdsGap: send text msg error: " + e.getMessage(), true);
                    return;
                }
            }
            ReportAffGap entity = buildDspAffGap(93, stdYesterday,
                    flatAdsImp, flatAdsCostStr, dspImp, dspCostStr, impGapStr, costGapStr);
            reportAffGapMapper.insert(entity);
        }
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
        return reportMapper.getReportSpentForFlatAds(93, DateUtil.yesterday().toDateStr());
    }

}
