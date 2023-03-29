package com.tecdo.log;

import com.tecdo.domain.biz.BidCreative;
import com.tecdo.domain.biz.log.RequestLog;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Device;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.Affiliate;
import com.tecdo.enums.biz.AdTypeEnum;
import com.tecdo.enums.openrtb.DeviceTypeEnum;
import com.tecdo.util.CreativeHelper;
import com.tecdo.util.FieldFormatHelper;
import com.tecdo.util.JsonHelper;

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

  public static void log(String bidId,
                         Imp imp,
                         BidRequest bidRequest,
                         Affiliate affiliate,
                         int rtaRequest,
                         int rtaRequestTrue) {
    RequestLog requestLog =
      buildRequestLog(bidId, imp, bidRequest, affiliate, rtaRequest, rtaRequestTrue);
    requestLogger.info(JsonHelper.toJSONString(requestLog));
  }

  private static RequestLog buildRequestLog(String bidId,
                                            Imp imp,
                                            BidRequest bidRequest,
                                            Affiliate affiliate,
                                            int rtaRequest,
                                            int rtaRequestTrue) {
    BidCreative bidCreative = CreativeHelper.getAdFormat(imp);
    Device device = bidRequest.getDevice();
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
                     .os(FieldFormatHelper.osFormat(device.getOs()))
                     .deviceMake(FieldFormatHelper.deviceMakeFormat(device.getMake()))
                     .bundleId(FieldFormatHelper.bundleIdFormat(bidRequest.getApp().getBundle()))
                     .country(FieldFormatHelper.countryFormat(device.getGeo().getCountry()))
                     .connectionType(device.getConnectiontype())
                     .deviceModel(FieldFormatHelper.deviceModelFormat(device.getModel()))
                     .osv(device.getOsv())
                     .carrier(device.getCarrier())
                     .pos(bidCreative.getPos())
                     .instl(imp.getInstl())
                     .domain(bidRequest.getApp().getDomain())
                     .cat(bidRequest.getApp().getCat())
                     .ip(Optional.ofNullable(device.getIp()).orElse(device.getIpv6()))
                     .ua(device.getUa())
                     .lang(FieldFormatHelper.languageFormat(device.getLanguage()))
                     .deviceId(device.getIfa())
                     .bidFloor(imp.getBidfloor().doubleValue())
                     .city(FieldFormatHelper.cityFormat(device.getGeo().getCity()))
                     .region(FieldFormatHelper.regionFormat(device.getGeo().getRegion()))
                     .deviceType(DeviceTypeEnum.of(device.getDevicetype()).name())
                     .screenWidth(device.getW())
                     .screenHeight(device.getH())
                     .screenPpi(device.getPpi())
                     .tagId(imp.getTagid())
                     .rtaRequest(rtaRequest)
                     .rtaRequestTrue(rtaRequestTrue)
                     .build();
  }
}
