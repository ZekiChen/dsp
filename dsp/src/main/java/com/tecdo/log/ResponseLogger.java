package com.tecdo.log;

import cn.hutool.core.date.DateUtil;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.CampaignRtaInfo;
import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.adm.api.doris.entity.GooglePlayApp;
import com.tecdo.domain.biz.BidCreative;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.biz.log.ResponseLog;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Device;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.enums.openrtb.DeviceTypeEnum;
import com.tecdo.fsm.task.handler.PriceCalcHandler;
import com.tecdo.util.CreativeHelper;
import com.tecdo.util.ExtHelper;
import com.tecdo.util.FieldFormatHelper;
import com.tecdo.util.JsonHelper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

/**
 * 构建 ResponseLog 并持久化至本地文件中
 * <p>
 * Created by Zeki on 2023/1/31
 */
@Component
@RequiredArgsConstructor
public class ResponseLogger {

    private final static Logger responseLogger = LoggerFactory.getLogger("response_log");

    public void log(AdDTOWrapper wrapper,
                    BidRequest bidRequest,
                    Affiliate affiliate,
                    GooglePlayApp googlePlayApp) {
        ResponseLog responseLog =
                buildResponseLog(wrapper, bidRequest, affiliate, googlePlayApp);
        responseLogger.info(JsonHelper.toJSONString(responseLog));
    }

    private ResponseLog buildResponseLog(AdDTOWrapper wrapper,
                                         BidRequest bidRequest,
                                         Affiliate affiliate,
                                         GooglePlayApp googlePlayApp) {
        Integer creativeId = CreativeHelper.getCreativeId(wrapper.getAdDTO().getAd());
        Imp imp = bidRequest.getImp()
                .stream()
                .filter(i -> i.getId().equalsIgnoreCase(wrapper.getImpId()))
                .findFirst()
                .get();
        Creative creative = wrapper.getAdDTO().getCreativeMap().get(creativeId);
        BidCreative bidCreative = CreativeHelper.getAdFormat(imp);
        Device device = bidRequest.getDevice();
        return ResponseLog.builder()
                .createTime(DateUtil.format(new Date(), "yyyy-MM-dd_HH"))
                .requestId(bidRequest.getId())
                .bidId(wrapper.getBidId())
                .advId(wrapper.getAdDTO().getAdv().getId())
                .advName(wrapper.getAdDTO().getAdv().getName())
                .bidStrategy(wrapper.getBidStrategyEnum().getType())
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
                        .orElse(-1))
                .bidPrice(PriceCalcHandler.convertToUsdByVivo(wrapper.getBidPrice(), affiliate).doubleValue())
                .pCtr(wrapper.getPCtr())
                .pCtrVersion(wrapper.getPCtrVersion())
                .pCvr(wrapper.getPCvr())
                .pCvrVersion(wrapper.getPCvrVersion())
                .landingPage(wrapper.getLandingPage())
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
                .bidFloor(wrapper.getBidfloor().doubleValue())
                .city(FieldFormatHelper.cityFormat(device.getGeo().getCity()))
                .region(FieldFormatHelper.regionFormat(device.getGeo().getRegion()))
                .deviceType(DeviceTypeEnum.of(device.getDevicetype()).name())
                .screenWidth(device.getW())
                .screenHeight(device.getH())
                .screenPpi(device.getPpi())
                .tagId(imp.getTagid())
                .rtaRequest(wrapper.getRtaRequest())
                .rtaRequestTrue(wrapper.getRtaRequestTrue())
                .creativeWidth(String.valueOf(creative.getWidth()))
                .creativeHeight(String.valueOf(creative.getHeight()))
                .categoryList(googlePlayApp.getCategoryList())
                .tagList(googlePlayApp.getTagList())
                .score(googlePlayApp.getScore())
                .downloads(googlePlayApp.getDownloads())
                .reviews(googlePlayApp.getReviews())
                .rtaToken(wrapper.getRtaToken())
                .bAdv(bidRequest.getBadv())
                .bApp(bidRequest.getBapp())
                .bCat(bidRequest.getBcat())
                .responseType(wrapper.getResponseTypeEnum().getType())
                .useDeeplink(wrapper.isUseDeeplink() ? 1 : 0)
                .bidAlgorithm(wrapper.getBidAlgorithmEnum().getType())
                .videoPlacement(imp.getVideo() != null ? imp.getVideo().getPlacement() : -1)
                .isRewarded(ExtHelper.isRewarded(bidRequest.getExt()) ? 1 : 0)
                .schain(ExtHelper.listSChain(bidRequest.getSource()))
                .impFrequency(wrapper.getImpFrequency())
                .clickFrequency(wrapper.getClickFrequency())
                .impFrequencyHour(wrapper.getImpFrequencyHour())
                .clickFrequencyHour(wrapper.getClickFrequencyHour())
                .bidStage(wrapper.getBidStageEnum().getType())
                .offerId(wrapper.getAdDTO().getAdGroup().getOfferId())
                .build();
    }
}
