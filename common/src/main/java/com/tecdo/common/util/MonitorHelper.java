package com.tecdo.common.util;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Zeki on 2023/5/29
 */
@Slf4j
public class MonitorHelper {

    // DSP监控告警通知群
    public final static String MONITOR_GROUP = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=1b914817-45ab-4b7d-9bec-92bc6408a69f";

    public static void logError(String msg) {
        logError(msg, false);
    }

    public static void logError(String msg, boolean send2Wechat) {
        log.error(msg);
        if (send2Wechat) {
            try {
                WeChatRobotUtils.sendTextMsg(MonitorHelper.MONITOR_GROUP, msg);
            } catch (Exception e) {
                logError(msg);
            }
        }
    }
}
