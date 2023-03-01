package com.tecdo.log;

import com.alibaba.fastjson2.JSON;
import com.tecdo.domain.biz.BidCreative;
import com.tecdo.domain.biz.log.RequestLog;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.Affiliate;
import com.tecdo.enums.biz.AdTypeEnum;
import com.tecdo.util.CreativeHelper;
import com.tecdo.util.FieldFormatHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;

import cn.hutool.core.date.DateUtil;

/**
 * 构建 RequestLog 并持久化至本地文件中
 * <p>
 * Created by Zeki on 2023/1/31
 */
public class RequestLogger {

  private final static Logger requestLogger = LoggerFactory.getLogger("request_log");

  public static void log(String bidId, Imp imp, BidRequest bidRequest, Affiliate affiliate) {
    RequestLog requestLog = buildRequestLog(bidId, imp, bidRequest, affiliate);
    requestLogger.info(JSON.toJSONString(requestLog));
  }

  private static RequestLog buildRequestLog(String bidId,
                                            Imp imp,
                                            BidRequest bidRequest,
                                            Affiliate affiliate) {
    BidCreative bidCreative = CreativeHelper.getAdFormat(imp);
    return RequestLog.builder()
                     .createTime(DateUtil.format(new Date(), "yyyy-MM-dd_HH"))
                     .bidId(bidId)
                     .affiliateId(affiliate.getId())
                     .affiliateName(affiliate.getName())
                     .adFormat(Optional.ofNullable(bidCreative.getType())
                                       .map(AdTypeEnum::of)
                                       .map(AdTypeEnum::getDesc)
                                       .orElse(null))
                     .adWidth(bidCreative.getWidth())
                     .adHeight(bidCreative.getHeight())
                     .os(FieldFormatHelper.osFormat(bidRequest.getDevice().getOs()))
                     .deviceMake(FieldFormatHelper.deviceMakeFormat(bidRequest.getDevice()
                                                                              .getMake()))
                     .bundleId(bidRequest.getApp().getBundle())
                     .country(FieldFormatHelper.countryFormat(bidRequest.getDevice()
                                                                        .getGeo()
                                                                        .getCountry()))
                     .connectionType(bidRequest.getDevice().getConnectiontype())
                     .deviceModel(FieldFormatHelper.deviceModelFormat(bidRequest.getDevice()
                                                                                .getModel()))
                     .osv(bidRequest.getDevice().getOsv())
                     .carrier(bidRequest.getDevice().getCarrier())
                     .pos(bidCreative.getPos())
                     .instl(imp.getInstl())
                     .domain(bidRequest.getApp().getDomain())
                     .cat(bidRequest.getApp().getCat())
                     .ip(Optional.ofNullable(bidRequest.getDevice().getIp())
                                 .orElse(bidRequest.getDevice().getIpv6()))
                     .ua(bidRequest.getDevice().getUa())
                     .lang(FieldFormatHelper.languageFormat(bidRequest.getDevice().getLanguage()))
                     .deviceId(bidRequest.getDevice().getIfa())
                     .bidFloor(imp.getBidfloor().doubleValue())
                     .build();
  }
}
