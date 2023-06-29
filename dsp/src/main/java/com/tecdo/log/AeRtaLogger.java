package com.tecdo.log;

import cn.hutool.core.date.DateUtil;
import com.tecdo.service.rta.ae.AeRtaInfoVO;
import com.tecdo.util.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zeki on 2023/5/5
 */
public class AeRtaLogger {

    private static final Logger aeRtaTrueLog = LoggerFactory.getLogger("ae_rta_true_log");

    public static void log(AeRtaInfoVO vo) {
        Map<String, Object> map = new HashMap<>();
        map.put("create_time", DateUtil.format(new Date(), "yyyy-MM-dd_HH"));
        map.put("time_millis", System.currentTimeMillis());
        map.put("adv_campaign_d", vo.getAdvCampaignId());
        map.put("target", vo.getTarget());
        aeRtaTrueLog.info(JsonHelper.toJSONString(map));
    }
}
