package com.tecdo.scheduler.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 监控类任务
 *
 * Created by Zeki on 2023/2/24
 */
@Slf4j
@Component
@AllArgsConstructor
public class MonitorJob {

    // DSP监控告警通知群
    private final static String SECRET = "1b914817-45ab-4b7d-9bec-92bc6408a69f";

    /**
     * 监控消息通知：每小时同步 渠道当天花费、DSP当天花费 到企微群
     *
     * 模板：
     * DSP渠道花费监控
     * 渠道：FlatAds
     * 渠道花费：xxx ---------------通过flatads report api获取
     * DSP预算：xxx ---------------目前该渠道总预算100$
     * DSP花费：xxx ---------------DSP在该渠道当前总花费
     */
//    @XxlJob("dailyCostFlatAds")
//    public void dailyCostFlatAds() {
//        XxlJobHelper.log("同步渠道当天花费、DSP当天花费到企微群");
//        WeChatRobotUtils.sendTextMsg(SECRET, "")
//    }

}
