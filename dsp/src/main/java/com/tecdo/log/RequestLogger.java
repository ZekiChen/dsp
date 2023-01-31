package com.tecdo.log;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import com.tecdo.domain.biz.log.RequestLog;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.entity.Affiliate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;

/**
 * 构建 RequestLog 并持久化至本地文件中
 * <p>
 * Created by Zeki on 2023/1/31
 */
public class RequestLogger {

    private final static Logger requestLogger = LoggerFactory.getLogger("request_log");

    public static void log(BidRequest bidRequest, Affiliate affiliate) {
        RequestLog requestLog = buildRequestLog(bidRequest, affiliate);
        requestLogger.info(JSON.toJSONString(requestLog));
    }

    private static RequestLog buildRequestLog(BidRequest bidRequest, Affiliate affiliate) {
        return RequestLog.builder()
                .createTime(DateUtil.format(new Date(), "yyyy-MM-dd_HH"))
                .bidId(bidRequest.getId())
                .affiliateId(affiliate.getId())
                .affiliateName(affiliate.getName())
                // TODO
                .adFormat()
                .adWidth()
                .adHeight()
                .os(bidRequest.getDevice().getOs())
                .deviceMake(bidRequest.getDevice().getMake())
                .bundleId(bidRequest.getApp().getBundle())
                .country(bidRequest.getDevice().getGeo().getCountry())
                .connectionType(bidRequest.getDevice().getConnectiontype())
                .deviceModel(bidRequest.getDevice().getModel())
                .osv(bidRequest.getDevice().getOsv())
                .carrier(bidRequest.getDevice().getCarrier())
                .pos()
                .instl(bidRequest.getImp().get(0).getInstl())
                .domain(bidRequest.getApp().getDomain())
                .cat(bidRequest.getApp().getCat())
                .ip(Optional.ofNullable(bidRequest.getDevice().getIp()).orElse(bidRequest.getDevice().getIpv6()))
                .ua(bidRequest.getDevice().getUa())
                .lang(bidRequest.getDevice().getLanguage())
                .deviceId(bidRequest.getDevice().getIfa())
                .bidFloor(bidRequest.getImp().get(0).getBidfloor().doubleValue())
                .build();
    }
}
