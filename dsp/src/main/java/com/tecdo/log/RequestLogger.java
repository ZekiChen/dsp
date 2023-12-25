package com.tecdo.log;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.constant.EventType;
import com.tecdo.domain.biz.BidCreative;
import com.tecdo.domain.biz.log.RequestLog;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Deal;
import com.tecdo.domain.openrtb.request.Device;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.adm.api.doris.entity.GooglePlayApp;
import com.tecdo.domain.openrtb.request.Video;
import com.tecdo.enums.openrtb.DeviceTypeEnum;
import com.tecdo.util.CreativeHelper;
import com.tecdo.util.ExtHelper;
import com.tecdo.util.FieldFormatHelper;
import com.tecdo.util.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                           int rtaRequestTrue,
                           GooglePlayApp googlePlayApp,
                           EventType exceptionEvent) {
        RequestLog requestLog = buildRequestLog(
                bidId,
                imp,
                bidRequest,
                affiliate,
                rtaRequest,
                rtaRequestTrue,
                googlePlayApp,
                exceptionEvent
        );
        requestLogger.info(JsonHelper.toJSONString(requestLog));
    }

    private static RequestLog buildRequestLog(String bidId,
                                              Imp imp,
                                              BidRequest bidRequest,
                                              Affiliate affiliate,
                                              int rtaRequest,
                                              int rtaRequestTrue,
                                              GooglePlayApp googlePlayApp,
                                              EventType exceptionEvent) {
        BidCreative bidCreative = CreativeHelper.getAdFormat(imp);
        Device device = bidRequest.getDevice();
        Float bidFloor = imp.getBidfloor();
        String dealIds = null;
        // 若存在pmp对象，求deals中bidFloor均值
        if (imp.getPmp() != null && CollUtil.isNotEmpty(imp.getPmp().getDeals())) {
            bidFloor = (float) imp.getPmp().getDeals().stream()
                    .mapToDouble(Deal::getBidfloor)
                    .average()
                    .orElse(0.0);
            dealIds = imp.getPmp().getDeals().stream()
                    .map(Deal::getId).collect(Collectors.joining(StrUtil.COMMA));
        }

        return RequestLog.builder()
                .createTime(DateUtil.format(new Date(), "yyyy-MM-dd_HH"))
                .requestId(bidRequest.getId())
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
                .bidFloor(bidFloor.doubleValue())
                .city(FieldFormatHelper.cityFormat(device.getGeo().getCity()))
                .region(FieldFormatHelper.regionFormat(device.getGeo().getRegion()))
                .deviceType(DeviceTypeEnum.of(device.getDevicetype()).name())
                .screenWidth(device.getW())
                .screenHeight(device.getH())
                .screenPpi(device.getPpi())
                .tagId(imp.getTagid())
                .rtaRequest(rtaRequest)
                .rtaRequestTrue(rtaRequestTrue)
                .categoryList(googlePlayApp.getCategoryList())
                .tagList(googlePlayApp.getTagList())
                .score(googlePlayApp.getScore())
                .downloads(googlePlayApp.getDownloads())
                .reviews(googlePlayApp.getReviews())
                .bAdv(bidRequest.getBadv())
                .bApp(bidRequest.getBapp())
                .bCat(bidRequest.getBcat())
                .videoPlacement(Optional.ofNullable(imp.getVideo()).map(Video::getPlacement).orElse(-1))
                .isRewarded(ExtHelper.isRewarded(bidRequest.getExt()) ? 1 : 0)
                .schain(ExtHelper.listSChain(bidRequest.getSource()))
                .exceptionEvent(Optional.ofNullable(exceptionEvent).map(Enum::name).orElse(null))
                .dealIds(dealIds)
                .build();
    }
}
