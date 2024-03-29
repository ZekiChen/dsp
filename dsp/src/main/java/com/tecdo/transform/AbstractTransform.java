package com.tecdo.transform;

import cn.hutool.core.collection.CollUtil;
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
import com.tecdo.service.CacheService;
import com.tecdo.service.response.VivoResponseBuilder;
import com.tecdo.service.rta.ae.AeHelper;
import com.tecdo.service.track.ClickTrackBuilder;
import com.tecdo.service.track.ImpTrackBuilder;
import com.tecdo.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public abstract class AbstractTransform implements IProtoTransform {

    @Value("${pac.notice.win-url}")
    private String winUrl;
    @Value("${pac.notice.imp-url}")
    private String impUrl;
    @Value("${pac.notice.click-url}")
    private String clickUrl;
    @Value("${pac.notice.loss-url}")
    private String lossUrl;
    @Value("${pac.notice.imp-info-url}")
    private String impInfoUrl;

    private final Random random = new Random();

    @Value("${pac.force.judge-url}")
    private String forceJudgeUrl;

    @Value("${pac.force.collect-feature-url}")
    private String collectFeatureUrl;

    @Value("${pac.force.collect-code-url}")
    private String collectCodeUrl;

    @Value("${pac.force.collect-error-url}")
    private String collectErrorUrl;

    @Value("${pac.force.collect-debug-url}")
    private String collectDebugUrl;

    @Value("${pac.force.delay-time-sd}")
    private Double sdForDelayTime;

    private final String AUCTION_PRICE_PARAM = "&bid_success_price=${AUCTION_PRICE}";
    private final String VIVO_AUCTION_PRICE_PARAM = "&bid_success_price=${WIN_PRICE}";
    private final String AUCTION_LOSS_PARAM = "&loss_code=${AUCTION_LOSS}";
    private final String CHECK_AUCTION_PRICE_PARAM = "&pricePaid=${AUCTION_PRICE}";

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

    @Value("${pac.response.pixalate.check-enable:false}")
    private boolean checkEnable;

    @Value("${pac.response.force-banner.debug-enable:false}")
    private boolean debugEnabled;

    @Value("${pac.response.pixalate.check-probability:1}")
    private double checkProbability;

    @Value("${pac.response.pixalate.check-max-count:50000}")
    private double checkMaxCount;

    @Value("${pac.response.pixalate.check-url}")
    private String checkUrl;

    @Value("${pac.response.pixalate.check-count-url}")
    private String checkCountUrl;

    @Value("${pac.affiliate.default-bidfloor:0.05f}")
    private Float defaultBibFloor;

    @Value("${pac.video.force.enabled:true}")
    private boolean videoForceEnabled;

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

    @Autowired
    private CacheService cacheService;

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
        return (Objects.equals(adDTO.getAd().getType(), AdTypeEnum.BANNER.getType())
                || (wrapper.getImage() != null && videoForceEnabled))
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
            imp.setBidfloor(imp.getBidfloor() == 0f ? defaultBibFloor : imp.getBidfloor());

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
        bid.setDealid(wrapper.getDealid());

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
        bid.setCrid(String.valueOf(creative.getExternalId()));
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
            String encryptParam = ParamHelper.encode(ClickUrlSecurityCipher.encryptString(oriParam,
                    encryptKey,
                    encryptIV));
            String baseEncryptUrl =
                    StringUtils.firstNonBlank(adDTO.getAdGroup().getEncryptClickUrlDomain(), baseDomain) +
                            basePath;
            clickUrl = baseEncryptUrl + encryptParam;
        }
        deepLink = deepLinkFormat(deepLink);

        String forceJudgeUrl = ParamHelper.urlFormat(this.forceJudgeUrl, null, wrapper, bidRequest, affiliate);
        String collectFeatureUrl = ParamHelper.urlFormat(this.collectFeatureUrl, null, wrapper, bidRequest, affiliate);
        String collectCodeUrl = ParamHelper.urlFormat(this.collectCodeUrl, null, wrapper, bidRequest, affiliate);
        String collectErrorUrl = ParamHelper.urlFormat(this.collectErrorUrl, null, wrapper, bidRequest, affiliate);
        String collectDebugUrl = ParamHelper.urlFormat(this.collectDebugUrl, null, wrapper, bidRequest, affiliate);
        String checkUrl = ParamHelper.urlFormat(this.checkUrl, null, wrapper, bidRequest, affiliate) + CHECK_AUCTION_PRICE_PARAM;
        String checkCountUrl = ParamHelper.urlFormat(this.checkCountUrl, null, wrapper, bidRequest, affiliate);

        // 构建 banner 流量的 adm 信息
        Object adm = null;
        boolean isForce = ResponseTypeEnum.FORCE.equals(getResponseType(forceLink, wrapper));
        boolean needCheck = checkEnable && random.nextDouble() * 100 < checkProbability
                && cacheService.getPixalateCache().getCheckCountToday() < checkMaxCount;
        switch (AdTypeEnum.of(adDTO.getAd().getType())) {
            case BANNER:
                if (isForce) {
                    adm = buildForceBannerAdm(wrapper, bidRequest, affiliate, adDTO, sign, impTrackList, clickTrackList,
                            deepLink, forceLink, clickUrl, forceJudgeUrl, collectFeatureUrl, collectCodeUrl, collectErrorUrl,
                            collectDebugUrl, checkUrl, checkCountUrl, needCheck, debugEnabled);
                } else {
                    adm = AdmGenerator.bannerAdm(clickUrl,
                            deepLink,
                            adDTO.getCreativeMap().get(adDTO.getAd().getImage()).getUrl(),
                            impTrackList,
                            clickTrackList,
                            ParamHelper.urlFormat(impInfoUrl, sign, wrapper, bidRequest, affiliate),
                            collectFeatureUrl,
                            collectCodeUrl,
                            collectErrorUrl,
                            checkUrl,
                            checkCountUrl,
                            needCheck);
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
                if (isForce) {
                    Object forceBannerAdm = buildForceBannerAdm(wrapper, bidRequest, affiliate, adDTO, sign,
                            impTrackList, clickTrackList, deepLink, forceLink, clickUrl, forceJudgeUrl,
                            collectFeatureUrl, collectCodeUrl, collectErrorUrl, collectDebugUrl,
                            checkUrl, checkCountUrl, needCheck, debugEnabled);
                    adm = AdmGenerator.forceVideoAdm(adDTO.getAd().getId(),
                            adDTO.getCreativeMap().get(adDTO.getAd().getVideo()),
                            wrapper.getImage(),
                            clickUrl, deepLink,
                            impTrackList, clickTrackList, forceBannerAdm.toString());
                } else {
                    adm = AdmGenerator.videoAdm(adDTO.getAd().getId(),
                            adDTO.getCreativeMap().get(adDTO.getAd().getVideo()),
                            wrapper.getImage(),
                            clickUrl, deepLink,
                            impTrackList, clickTrackList);
                }
                break;
        }

        return adm;
    }

    private Object buildForceBannerAdm(AdDTOWrapper wrapper, BidRequest bidRequest, Affiliate affiliate, AdDTO adDTO,
                                       String sign, List<String> impTrackList, List<String> clickTrackList,
                                       String deepLink, String forceLink, String clickUrl, String forceJudgeUrl,
                                       String collectFeatureUrl, String collectCodeUrl, String collectErrorUrl,
                                       String collectDebugUrl, String checkUrl, String checkCountUrl,
                                       boolean needCheck, boolean debugEnabled) {
        Object adm;
        Double delayTimeMean = affiliate.getAutoRedirectDelayTime();
        double delayTime = 0;
        if (delayTimeMean != null && delayTimeMean > 0) {
            delayTime = delayTimeMean + random.nextGaussian() * sdForDelayTime;
        }
        adm = //
                AdmGenerator.forceBannerAdm(clickUrl,
                        deepLink,
                        adDTO.getCreativeMap().get(adDTO.getAd().getImage()).getUrl(),
                        impTrackList,
                        clickTrackList,
                        ParamHelper.urlFormat(impInfoUrl, sign, wrapper, bidRequest, affiliate),
                        forceLink,
                        forceJudgeUrl,
                        collectFeatureUrl,
                        collectCodeUrl,
                        collectErrorUrl,
                        collectDebugUrl,
                        delayTime,
                        checkUrl,
                        checkCountUrl,
                        needCheck,
                        debugEnabled);
        return adm;
    }
}
