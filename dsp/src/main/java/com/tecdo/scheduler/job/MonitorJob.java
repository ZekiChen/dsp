package com.tecdo.scheduler.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.zhxu.okhttps.HttpResult;
import cn.zhxu.okhttps.OkHttps;
import com.alibaba.fastjson2.JSON;
import com.tecdo.domain.foreign.flatads.FlatAdsReportVO;
import com.tecdo.domain.foreign.flatads.FlatAdsResponse;
import com.tecdo.service.init.BudgetManager;
import com.tecdo.util.WeChatRobotUtils;
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
    private final static String SECRET = "1b914817-45ab-4b7d-9bec-92bc6408a69f";

    @Value("${foreign.flat-ads.report-url}")
    private String flatAdsReportUrl;

    private final BudgetManager budgetManager;

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
            paramMap.put("condition", JSON.toJSONString(conditionMap));
            HttpResult result = OkHttps.sync(flatAdsReportUrl).addBodyPara(paramMap).post();
            if (result.isSuccessful()) {
                FlatAdsResponse response = result.getBody().toBean(FlatAdsResponse.class);
                List<FlatAdsReportVO> reportVOs = response.getData();
                if (CollUtil.isEmpty(reportVOs)) {
                    log.error("call FlatAds report error, data is empty");
                    return;
                }
                FlatAdsReportVO reportVO = reportVOs.get(0);
                Double revenue = reportVO.getGrossRevenue();
                // TODO  获取 FlatAds 对应的 campaignId
                String campaignId = "";
                Double campaignCost = budgetManager.getCampaignCost(campaignId, 0D);

                String msg = "DSP渠道花费监控\n"
                        + "渠道：FlatAds\n"
                        + "渠道花费：" + revenue + "\n"
                        + "DSP预算：100$\n"
                        + "DSP花费：" + campaignCost;

                WeChatRobotUtils.sendTextMsg(SECRET, msg);
            } else {
                log.error("call FlatAds report error, status: {}", result.getStatus());
                WeChatRobotUtils.sendTextMsg(SECRET, "call FlatAds report url error, status: " + result.getStatus());
            }
        } catch (Exception e) {
            log.error("job execute error", e);
        }
    }

    /**
     * 超预算报警：当满足以下条件，立即触发电话告警，通知以下联系人
     *
     * a. 条件：渠道花费>2倍DSP预算  或  DSP花费>2倍DSP预算
     * b. 告警通知对象：Eric、Zeki、Dawin
     */

}
