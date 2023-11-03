package com.tecdo.transform;

import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.domain.openrtb.request.n.NativeRequest;
import com.tecdo.domain.openrtb.request.n.NativeRequestWrapper;
import com.tecdo.domain.openrtb.response.Bid;
import com.tecdo.domain.openrtb.response.BidResponse;
import com.tecdo.domain.openrtb.response.SeatBid;
import com.tecdo.domain.openrtb.response.n.NativeResponse;
import com.tecdo.domain.openrtb.response.n.NativeResponseWrapper;
import com.tecdo.enums.biz.VideoProtocolEnum;
import com.tecdo.service.response.VivoResponseBuilder;
import com.tecdo.service.rta.ae.AeHelper;
import com.tecdo.service.track.ClickTrackBuilder;
import com.tecdo.service.track.ImpTrackBuilder;
import com.tecdo.util.AdmGenerator;
import com.tecdo.util.ClickUrlSecurityCipher;
import com.tecdo.util.CreativeHelper;
import com.tecdo.util.JsonHelper;
import com.tecdo.util.ParamHelper;
import com.tecdo.util.SignHelper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.hutool.extra.spring.SpringUtil;

@Component
public abstract class AbstractTransform implements IProtoTransform {

    private final String winUrl = SpringUtil.getProperty("pac.notice.win-url");
    private final String impUrl = SpringUtil.getProperty("pac.notice.imp-url");
    private final String clickUrl = SpringUtil.getProperty("pac.notice.click-url");
    private final String lossUrl = SpringUtil.getProperty("pac.notice.loss-url");
    private final String impInfoUrl = SpringUtil.getProperty("pac.notice.imp-info-url");

    private final String AUCTION_PRICE_PARAM = "&bid_success_price=${AUCTION_PRICE}";
    private final String VIVO_AUCTION_PRICE_PARAM = "&bid_success_price=${WIN_PRICE}";
    private final String AUCTION_LOSS_PARAM = "&loss_code=${AUCTION_LOSS}";

    @Value("${pac.ae.rta.deeplink.ratio}")
    private Double aeDeeplinkRatio;

    @Value("${pac.url.encrypt.key}")
    private String encryptKey;

    @Value("${pac.url.encrypt.iv}")
    private String encryptIV;

    @Value("${pac.url.encrypt.basePath:/cpd?pd=}")
    private String basePath;

    @Value("${pac.url.encrypt.baseDomain}")
    private String baseDomain;

    public abstract boolean forceBannerEnable();

    public abstract String deepLinkFormat(String deepLink);

    public abstract boolean useBurl();

    public abstract boolean useLossUrl();

    public abstract boolean buildAdmByImmobi();

    @Autowired
    private ImpTrackBuilder impTrackBuilder;
    @Autowired
    private ClickTrackBuilder clickTrackBuilder;

    @Autowired
    private VivoResponseBuilder vivoResponseBuilder;

    @Override
    public ResponseTypeEnum getResponseType(String forceLink, AdDTOWrapper wrapper) {
        if (isForceJump(forceLink, wrapper)) {
            wrapper.setResponseTypeEnum(ResponseTypeEnum.FORCE);
            return ResponseTypeEnum.FORCE;
        }
        return ResponseTypeEnum.NORMAL;
    }

    private boolean isForceJump(String forceLink, AdDTOWrapper wrapper) {
        AdDTO adDTO = wrapper.getAdDTO();
        return Objects.equals(adDTO.getAd().getType(), AdTypeEnum.BANNER.getType())
                && forceBannerEnable()
                && adDTO.getAdGroup().getForceJumpEnable()
                && StringUtils.isNotBlank(forceLink)
                && wrapper.getRandom() < adDTO.getAdGroup().getForceJumpRatio();
    }

    @Override
    public BidRequest requestTransform(String req) {
        BidRequest bidRequest = JsonHelper.parseObject(req, BidRequest.class);
        if (bidRequest == null || CollectionUtils.isEmpty(bidRequest.getImp())) {
            return bidRequest;
        }
        for (Imp imp : bidRequest.getImp()) {
            if (imp.getNative1() != null) {
                String nativeRequestString = imp.getNative1().getRequest();
                // 有些adx没有按照协议中ver的定义，比如传了1.2，但还是有native的wrapper
                NativeRequestWrapper nativeRequestWrapper =
                        JsonHelper.parseObject(nativeRequestString, NativeRequestWrapper.class);
                if (nativeRequestWrapper != null && nativeRequestWrapper.getNativeRequest() != null) {
                    imp.getNative1().setNativeRequestWrapper(nativeRequestWrapper);
                    imp.getNative1().setNativeRequest(nativeRequestWrapper.getNativeRequest());
                } else {
                    NativeRequest nativeRequest =
                            JsonHelper.parseObject(nativeRequestString, NativeRequest.class);
                    imp.getNative1().setNativeRequest(nativeRequest);
                }
            }
        }
        return bidRequest;
    }

    public BidResponse responseTransform(Map<String, AdDTOWrapper> impBidAdMap,
                                         BidRequest bidRequest,
                                         Affiliate affiliate) {
        List<Bid> bids = impBidAdMap.values().stream()
                .map(w -> buildBid(bidRequest, affiliate, w))
                .collect(Collectors.toList());
        SeatBid seatBid = new SeatBid();
        seatBid.setBid(bids);
        seatBid.setSeat("agency");

        BidResponse bidResponse = new BidResponse();
        bidResponse.setId(bidRequest.getId());
        bidResponse.setBidid(bids.get(0).getId());
        if (affiliate.getApi().equals(ProtoTransformFactory.VIVO)) {
            bidResponse.setSeatBid(Collections.singletonList(seatBid));
        } else {
            bidResponse.setSeatbid(Collections.singletonList(seatBid));
        }
        return bidResponse;
    }

    private Bid buildBid(BidRequest bidRequest, Affiliate affiliate, AdDTOWrapper wrapper) {
        AdDTO adDTO = wrapper.getAdDTO();
        String bidId = wrapper.getBidId();
        Integer creativeId = CreativeHelper.getCreativeId(adDTO.getAd());
        Creative creative = adDTO.getCreativeMap().get(creativeId);

        Bid bid = new Bid();
        bid.setId(bidId);
        bid.setPrice(wrapper.getBidPrice().floatValue());

        String sign = SignHelper.digest(bidId, adDTO.getCampaign().getId().toString());
        String winUrl = ParamHelper.urlFormat(this.winUrl, sign, wrapper, bidRequest, affiliate);
        winUrl = winUrl.concat(affiliate.getApi().equals(ProtoTransformFactory.VIVO) ?
                VIVO_AUCTION_PRICE_PARAM : AUCTION_PRICE_PARAM);
        // 判断当前 ADX 是否支持 loss notice，支持则将回调的 loss ep 设置至 lurl
        if (useLossUrl()) {
            String lossUrl =
                    ParamHelper.urlFormat(this.lossUrl, sign, wrapper, bidRequest, affiliate) + AUCTION_LOSS_PARAM;
            bid.setLurl(lossUrl);
        }
        // 判断当前 ADX 是使用 计费通知burl/胜出通知nurl，将回调的 win ep 设置至 burl/nurl
        if (useBurl()) {
            bid.setBurl(SignHelper.urlAddSign(winUrl, sign));
        } else {
            bid.setNurl(SignHelper.urlAddSign(winUrl, sign));
        }

        if (affiliate.getApi().equals(ProtoTransformFactory.VIVO)) {
            bid.setImpId(wrapper.getImpId());
            String sysImpTrack = this.impUrl + VIVO_AUCTION_PRICE_PARAM;
            String sysClickTrack = this.clickUrl;
            bid.setAdm(vivoResponseBuilder.buildAdm(wrapper.getAdDTO().getCampaign().getPackageName()));
            bid.setTrackUrls(vivoResponseBuilder.buildTracks(sysImpTrack, sysClickTrack, wrapper, bidRequest, affiliate, sign));
        } else if (Objects.equals(adDTO.getAd().getType(), AdTypeEnum.NATIVE.getType()) && buildAdmByImmobi()) {
            bid.setImpid(wrapper.getImpId());
            bid.setAdmobject(buildAdm(wrapper, bidRequest, affiliate));
        } else {
            bid.setImpid(wrapper.getImpId());
            bid.setAdm(buildAdm(wrapper, bidRequest, affiliate));
        }

        bid.setAdid(String.valueOf(adDTO.getAd().getId()));
        bid.setAdomain(Collections.singletonList(adDTO.getCampaign().getDomain()));
        bid.setBundle(adDTO.getCampaign().getPackageName());
        bid.setIurl(creative.getUrl());
        if (creative.getCatIab() != null) {
            bid.setCat(Arrays.asList(StringUtils.split(creative.getCatIab(), ",")));
        }
        bid.setW(creative.getWidth());
        bid.setH(creative.getHeight());
        bid.setCid(String.valueOf(adDTO.getCampaign().getId()));
        bid.setCrid(String.valueOf(creativeId));
        if (AdTypeEnum.VIDEO.getType() == creative.getType()) {
            bid.setProtocol(VideoProtocolEnum.VAST_4.getType());
        }
        return bid;
    }

    private Object buildAdm(AdDTOWrapper wrapper, BidRequest bidRequest, Affiliate affiliate) {
        AdDTO adDTO = wrapper.getAdDTO();
        String sign = SignHelper.digest(wrapper.getBidId(), adDTO.getCampaign().getId().toString());

        // 构建展示追踪链
        String systemImpTrack = this.impUrl + AUCTION_PRICE_PARAM;
        List<String> impTrackList = impTrackBuilder.build(systemImpTrack, wrapper, sign, bidRequest, affiliate);
        // 构建点击追踪链
        String systemClickTrack = this.clickUrl;
        List<String> clickTrackList = clickTrackBuilder.build(systemClickTrack, wrapper, sign, bidRequest, affiliate);

        String deepLink =
                ParamHelper.urlFormat(adDTO.getAdGroup().getDeeplink(), sign, wrapper, bidRequest, affiliate);
        String forceLink =
                ParamHelper.urlFormat(adDTO.getAdGroup().getForceLink(), sign, wrapper, bidRequest, affiliate);

        String clickUrl;
        if (AeHelper.isAeAudience(wrapper)) {  // 当前流量命中 AE RTA 受众
            String deviceId = bidRequest.getDevice().getIfa();
            clickUrl = AeHelper.landingPageFormat(wrapper.getLandingPage(), wrapper, sign, deviceId, affiliate.getId());
            if (AeHelper.isUseDeeplink(wrapper, aeDeeplinkRatio)) {
                deepLink = AeHelper.landingPageFormat(wrapper.getDeeplink(), wrapper, sign, deviceId, affiliate.getId());
                wrapper.setUseDeeplink(true);
            }
            forceLink = clickUrl;
        } else {
            clickUrl = ParamHelper.urlFormat(adDTO.getAdGroup().getClickUrl(), sign, wrapper, bidRequest, affiliate);
        }
        // if adGroup encrypt clickUrl enable,replace domain and encrypt clickUrl
        if (adDTO.getAdGroup().getEncryptClickUrlEnable()) {
            //get original param string
            String oriParam = ParamHelper.getUriParamAsString(clickUrl);
            String encryptParam =
              ClickUrlSecurityCipher.encryptString(oriParam, encryptKey, encryptIV);
            String baseEncryptUrl =
              StringUtils.firstNonBlank(adDTO.getAdGroup().getEncryptClickUrlDomain(), baseDomain) +
              basePath;
            clickUrl = baseEncryptUrl + encryptParam;
        }
        deepLink = deepLinkFormat(deepLink);

        // 构建 banner 流量的 adm 信息
        Object adm = null;
        switch (AdTypeEnum.of(adDTO.getAd().getType())) {
            case BANNER:
                if (ResponseTypeEnum.FORCE.equals(getResponseType(forceLink, wrapper))) {
                    adm = //
                            AdmGenerator.forceBannerAdm(clickUrl,
                                    deepLink,
                                    adDTO.getCreativeMap().get(adDTO.getAd().getImage()).getUrl(),
                                    impTrackList,
                                    clickTrackList,
                                    ParamHelper.urlFormat(impInfoUrl, sign, wrapper, bidRequest, affiliate),
                                    forceLink);
                } else {
                    adm = AdmGenerator.bannerAdm(clickUrl,
                            deepLink,
                            adDTO.getCreativeMap().get(adDTO.getAd().getImage()).getUrl(),
                            impTrackList,
                            clickTrackList,
                            ParamHelper.urlFormat(impInfoUrl, sign, wrapper, bidRequest, affiliate));
                }
                break;
            case NATIVE:
                List<Imp> impList = bidRequest.getImp();
                Imp imp = impList.stream()
                        .filter(i -> Objects.equals(i.getId(), wrapper.getImpId()))
                        .findFirst()
                        .get();
                NativeResponse nativeResponse = //
                        AdmGenerator.nativeAdm(imp.getNative1().getNativeRequest(),
                                adDTO,
                                clickUrl,
                                deepLink,
                                impTrackList,
                                clickTrackList);
                if (buildAdmByImmobi()) {
                    NativeResponseWrapper nativeResponseWrapper = new NativeResponseWrapper();
                    nativeResponseWrapper.setNativeResponse(nativeResponse);
                    adm = nativeResponseWrapper;
                } else {
                    if (imp.getNative1().getNativeRequestWrapper() != null) {
                        NativeResponseWrapper nativeResponseWrapper = new NativeResponseWrapper();
                        nativeResponseWrapper.setNativeResponse(nativeResponse);
                        adm = JsonHelper.toJSONString(nativeResponseWrapper);
                    } else {
                        adm = JsonHelper.toJSONString(nativeResponse);
                    }
                }
                break;
            case VIDEO:
                adm = AdmGenerator.videoAdm(adDTO.getAd().getId(),
                        adDTO.getCreativeMap().get(adDTO.getAd().getVideo()),
                        clickUrl, deepLink,
                        impTrackList, clickTrackList);
                break;
        }

        return adm;
    }
}
