package com.tecdo.log;

import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Device;
import com.tecdo.util.FieldFormatHelper;
import com.tecdo.util.JsonHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import cn.hutool.core.date.DateUtil;

/**
 * Created by Zeki on 2023/6/13
 */
public class ValidateLogger {

    private static final Logger requestValidateLog = LoggerFactory.getLogger("validate_request_log");

    public static void log(String blockedType, BidRequest bidRequest, Affiliate affiliate, boolean filter) {
        log(blockedType, bidRequest, affiliate, filter, null, null);
    }

    public static void log(String blockedType, BidRequest bidRequest, Affiliate affiliate,
                           boolean filter, Double ipProbability, Double deviceIdProbability) {
        Map<String, Object> map = new HashMap<>();
        Device device = bidRequest.getDevice();
        map.put("create_time", DateUtil.format(new Date(), "yyyy-MM-dd_HH"));
        map.put("time_millis", System.currentTimeMillis());
        map.put("affiliate_id", affiliate.getId());
        map.put("affiliate_name", affiliate.getName());
        map.put("bundle_id", FieldFormatHelper.bundleIdFormat(bidRequest.getApp().getBundle()));
        map.put("os", FieldFormatHelper.osFormat(device.getOs()));
        map.put("osv", device.getOsv());
        map.put("ip", Optional.ofNullable(device.getIp()).orElse(device.getIpv6()));
        map.put("ua", device.getUa());
        map.put("lang", FieldFormatHelper.languageFormat(device.getLanguage()));
        map.put("device_id", device.getIfa());
        map.put("device_make", FieldFormatHelper.deviceMakeFormat(device.getMake()));
        map.put("device_model", FieldFormatHelper.deviceModelFormat(device.getModel()));
        map.put("connection_type", device.getConnectiontype());
        map.put("country", FieldFormatHelper.countryFormat(device.getGeo().getCountry()));
        map.put("city", FieldFormatHelper.cityFormat(device.getGeo().getCity()));
        map.put("region", FieldFormatHelper.regionFormat(device.getGeo().getRegion()));
        map.put("screen_width", device.getW());
        map.put("screen_height", device.getH());
        map.put("screen_ppi", device.getPpi());
        map.put("blocked_type", blockedType);
        map.put("filter", filter ? 1 : 0);
        map.put("ip_probability", ipProbability);
        map.put("device_id_probability", deviceIdProbability);
        requestValidateLog.info(JsonHelper.toJSONString(map));
    }

}
