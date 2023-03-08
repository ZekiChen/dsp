package com.tecdo.log;

import com.alibaba.fastjson2.JSON;
import com.tecdo.domain.biz.BidCreative;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.biz.log.ResponseLog;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.CampaignRtaInfo;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.util.CreativeHelper;
import com.tecdo.util.FieldFormatHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;

import cn.hutool.core.date.DateUtil;

/**
 * 构建 ResponseLog 并持久化至本地文件中
 * <p>
 * Created by Zeki on 2023/1/31
 */
public class ResponseLogger {

  private final static Logger responseLogger = LoggerFactory.getLogger("response_log");

  public static void log(AdDTOWrapper wrapper, BidRequest bidRequest, Affiliate affiliate) {
    ResponseLog responseLog = buildResponseLog(wrapper, bidRequest, affiliate);
    responseLogger.info(JSON.toJSONString(responseLog));
  }

  private static ResponseLog buildResponseLog(AdDTOWrapper wrapper,
                                              BidRequest bidRequest,
                                              Affiliate affiliate) {
    Integer creativeId = CreativeHelper.getCreativeId(wrapper.getAdDTO().getAd());
    Imp imp = bidRequest.getImp()
                        .stream()
                        .filter(i -> i.getId().equalsIgnoreCase(wrapper.getImpId()))
                        .findFirst()
                        .get();
    BidCreative bidCreative = CreativeHelper.getAdFormat(imp);
    return ResponseLog.builder()
                      .createTime(DateUtil.format(new Date(), "yyyy-MM-dd_HH"))
                      .bidId(wrapper.getBidId())
                      .campaignId(wrapper.getAdDTO().getCampaign().getId())
                      .campaignName(wrapper.getAdDTO().getCampaign().getName())
                      .adGroupId(wrapper.getAdDTO().getAdGroup().getId())
                      .adGroupName(wrapper.getAdDTO().getAdGroup().getName())
                      .adId(wrapper.getAdDTO().getAd().getId())
                      .adName(wrapper.getAdDTO().getAd().getName())
                      .creativeId(creativeId)
                      .creativeName(wrapper.getAdDTO().getCreativeMap().get(creativeId).getName())
                      .packageName(wrapper.getAdDTO().getCampaign().getPackageName())
                      .category(wrapper.getAdDTO().getCampaign().getCategory())
                      .feature(Optional.ofNullable(wrapper.getAdDTO().getCampaignRtaInfo())
                                       .map(CampaignRtaInfo::getRtaFeature)
                                       .orElse(null))
                      .bidPrice(wrapper.getBidPrice())
                      .pCtr(wrapper.getPCtr())
                      .pCtrVersion(wrapper.getPCtrVersion())
                      .affiliateId(affiliate.getId())
                      .affiliateName(affiliate.getName())
                      .adFormat(Optional.ofNullable(bidCreative.getType())
                                        .map(AdTypeEnum::of)
                                        .map(AdTypeEnum::getDesc)
                                        .orElse(null))
                      .adWidth(bidCreative.getWidth())
                      .adHeight(bidCreative.getHeight())
                      .os(bidRequest.getDevice().getOs())
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
