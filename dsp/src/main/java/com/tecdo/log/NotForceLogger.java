package com.tecdo.log;

import cn.hutool.core.date.DateUtil;
import com.tecdo.constant.RequestKeyByForce;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.util.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Zeki on 2023/11/24
 */
public class NotForceLogger {

    private static final Logger logger = LoggerFactory.getLogger("not_force_log");

    public static void log(HttpRequest httpRequest, int code) {
        String bidId = httpRequest.getParamAsStr(RequestKeyByForce.BID_ID);
        String affiliateId = httpRequest.getParamAsStr(RequestKeyByForce.AFFILIATE_ID);
        String country = httpRequest.getParamAsStr(RequestKeyByForce.COUNTRY);
        String bundle = httpRequest.getParamAsStr(RequestKeyByForce.BUNDLE);
        String schain = httpRequest.getParamAsStr(RequestKeyByForce.SCHAIN);
        Integer adGroupId = httpRequest.getParamAsInteger(RequestKeyByForce.AD_GROUP_ID);

        Map<String, Object> map = new HashMap<>();
        map.put("create_time", DateUtil.format(new Date(), "yyyy-MM-dd_HH"));
        map.put("time_millis", System.currentTimeMillis());
        map.put("bid_id", bidId);

        map.put("affiliate_id", affiliateId);
        map.put("country", country);
        map.put("bundle", bundle);
        map.put("schain", schain);
        map.put("ad_group_id", adGroupId);
        map.put("code", code);

        logger.info(JsonHelper.toJSONString(map));
    }
}
