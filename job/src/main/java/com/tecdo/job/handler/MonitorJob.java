package com.tecdo.job.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.zhxu.okhttps.HttpResult;
import cn.zhxu.okhttps.OkHttps;
import com.tecdo.job.domain.flatads.FlatAdsReportVO;
import com.tecdo.job.domain.flatads.FlatAdsResponse;
import com.tecdo.job.service.init.BudgetManager;
import com.tecdo.job.service.init.CampaignManager;
import com.tecdo.job.util.WeChatRobotUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 监控类任务
 *
 * Created by Zeki on 2023/2/24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorJob {

    // DSP监控告警通知群
    private final static String MONITOR_GROUP = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=1b914817-45ab-4b7d-9bec-92bc6408a69f";
    // 电话告警通知
//    private final static String PHONE_CALL = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=667ab4ef-2bf0-404c-af6c-936fd7f795d6";

    @Value("${foreign.flat-ads.report-url}")
    private String flatAdsReportUrl;

    private final BudgetManager budgetManager;
    private final CampaignManager campaignManager;

    /**
     * 每小时同步 渠道当天花费、DSP当天花费 到企微群
     *
     * 模板：
     * DSP渠道花费监控
     * 渠道：FlatAds
     * 渠道花费：xxx（通过flatads report api获取）
     * DSP预算：xxx（目前该渠道总预算100$）
     * DSP花费：xxx（DSP在该渠道当前总花费）
     */
    @XxlJob("dailyCostFlatAds")
    public void dailyCostFlatAds() {
        try {
            XxlJobHelper.log("同步渠道当天花费、DSP当天花费到企微群");
            String today = DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN);
            Map<String, String> conditionMap = MapUtil.newHashMap();
            conditionMap.put("dt_from", today);
            conditionMap.put("dt_to", today);
            Map<String, Object> paramMap = MapUtil.newHashMap();
            paramMap.put("condition", conditionMap);
            HttpResult result = OkHttps.sync(flatAdsReportUrl).bodyType(OkHttps.JSON).addBodyPara(paramMap).post();
            if (result.isSuccessful()) {
                FlatAdsResponse response = result.getBody().toBean(FlatAdsResponse.class);
                List<FlatAdsReportVO> reportVOs = response.getData();
                if (CollUtil.isEmpty(reportVOs)) {
                    logError("call FlatAds report error, data is empty");
                    return;
                }
                FlatAdsReportVO reportVO = reportVOs.get(0);
                Double revenue = reportVO.getGrossRevenue();
                if (revenue == null) {
                    logError("call FlatAds report error, revenue is null");
                    return;
                }
                String flatAdsRevenueStr = NumberUtil.round(revenue, 2).toString();  // 保留2位小数
                Double dspCampaignCost = doGetDailyCostFlatAds();
                Double dspBudget = campaignManager.dailyBudget();
                String dspBudgetStr = NumberUtil.round(dspBudget, 2).toString();

                // 超预算电话报警：当满足 渠道花费/DSP花费 > DSP预算 条件时，立即通知 Eric、Zeki、Dawin
                if (revenue > dspBudget || dspCampaignCost > dspBudget) {
                    // TODO
                }

                String msg = "DSP渠道花费监控\n"
                        + "渠道：FlatAds\n"
                        + "渠道花费：" + flatAdsRevenueStr + "\n"
                        + "DSP预算：" + dspBudgetStr + "$\n"
                        + "DSP花费：" + dspCampaignCost;

                WeChatRobotUtils.sendTextMsg(MONITOR_GROUP, msg);
            } else {
                logError("call FlatAds report error, status: " + result.getStatus(), true);
            }
        } catch (Exception e) {
            log.error("job execute error", e);
        }
    }

    // 目前 flatAds 对应的 campaignId 为 3～15，暂时先写死
    private Double doGetDailyCostFlatAds() {
        Double sum = 0d;
        for (int campaignId = 3; campaignId <= 15; campaignId++) {
            sum += budgetManager.getCampaignCost(String.valueOf(campaignId), 0d);
        }
        return sum;
    }

    private static void logError(String msg) {
        logError(msg, false);
    }

    private static void logError(String msg, boolean send2Wechat) {
        log.error(msg);
        XxlJobHelper.handleFail(msg);
        if (send2Wechat) {
            try {
                WeChatRobotUtils.sendTextMsg(MONITOR_GROUP, msg);
            } catch (Exception e) {
                logError(msg);
            }
        }
    }
}