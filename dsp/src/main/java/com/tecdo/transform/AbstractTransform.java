package com.tecdo.transform;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.constant.FormatKey;
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
import com.tecdo.service.rta.ae.AeHelper;
import com.tecdo.util.AdmGenerator;
import com.tecdo.util.CreativeHelper;
import com.tecdo.util.JsonHelper;
import com.tecdo.util.SignHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

@Component
public abstract class AbstractTransform implements IProtoTransform {

  private final String winUrl = SpringUtil.getProperty("pac.notice.win-url");
  private final String impUrl = SpringUtil.getProperty("pac.notice.imp-url");
  private final String clickUrl = SpringUtil.getProperty("pac.notice.click-url");
  private final String lossUrl = SpringUtil.getProperty("pac.notice.loss-url");
  private final String AUCTION_PRICE_PARAM = "&bid_success_price=${AUCTION_PRICE}";
  private final String AUCTION_LOSS_PARAM = "&loss_code=${AUCTION_LOSS}";
  private final String impInfoUrl = SpringUtil.getProperty("pac.notice.imp-info-url");

  @Value("${pac.ae.rta.deeplink.ratio}")
  private Double aeDeeplinkRatio;

  public abstract String deepLinkFormat(String deepLink);

  public abstract boolean useBurl();

  public abstract boolean buildAdmObject();

  public abstract boolean useLossUrl();

  public abstract boolean forceBannerEnable();

  @Override
  public ResponseTypeEnum getResponseType(String forceLink, AdDTOWrapper wrapper) {
    AdDTO adDTO = wrapper.getAdDTO();
    if (Objects.equals(adDTO.getAd().getType(), AdTypeEnum.BANNER.getType())
            && forceBannerEnable()
            && adDTO.getAdGroup().getForceJumpEnable()
            && StringUtils.isNotBlank(forceLink)
            && Math.random() < adDTO.getAdGroup().getForceJumpRatio()) {
        wrapper.setResponseTypeEnum(ResponseTypeEnum.FORCE);
        return ResponseTypeEnum.FORCE;
    }
    return ResponseTypeEnum.NORMAL;
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

  public BidResponse responseTransform(AdDTOWrapper wrapper,
                                       BidRequest bidRequest,
                                       Affiliate affiliate) {
    AdDTO adDTO = wrapper.getAdDTO();
    String bidId = wrapper.getBidId();
    Integer creativeId = CreativeHelper.getCreativeId(adDTO.getAd());
    Creative creative = adDTO.getCreativeMap().get(creativeId);

    Bid bid = new Bid();
    bid.setId(bidId);
    bid.setImpid(wrapper.getImpId());
    bid.setPrice(wrapper.getBidPrice().floatValue());

    String sign = SignHelper.digest(bidId, adDTO.getCampaign().getId().toString());
    String winUrl =
      urlFormat(this.winUrl, sign, wrapper, bidRequest, affiliate) + AUCTION_PRICE_PARAM;
    // 判断当前 ADX 是否支持 loss notice，支持则将回调的 loss ep 设置至 lurl
    if (useLossUrl()) {
      String lossUrl =
        urlFormat(this.lossUrl, sign, wrapper, bidRequest, affiliate) + AUCTION_LOSS_PARAM;
      bid.setLurl(lossUrl);
    }
    // 判断当前 ADX 是使用 计费通知burl/胜出通知nurl，将回调的 win ep 设置至 burl/nurl
    if (useBurl()) {
      bid.setBurl(SignHelper.urlAddSign(winUrl, sign));
    } else {
      bid.setNurl(SignHelper.urlAddSign(winUrl, sign));
    }

    // 如果当前 native 流量来自 inmobi ，则使用 inmobi 的专用字段 admobject 填充 adm 信息
    if (Objects.equals(adDTO.getAd().getType(), AdTypeEnum.NATIVE.getType()) && buildAdmObject()) {
      bid.setAdmobject(buildAdm(wrapper, bidRequest, affiliate));
    } else {
      bid.setAdm((String) buildAdm(wrapper, bidRequest, affiliate));
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

    SeatBid seatBid = new SeatBid();
    seatBid.setBid(Collections.singletonList(bid));
    seatBid.setSeat("agency");

    BidResponse bidResponse = new BidResponse();
    bidResponse.setId(bidRequest.getId());
    bidResponse.setBidid(bidId);
    bidResponse.setSeatbid(Collections.singletonList(seatBid));
    return bidResponse;
  }

  private Object buildAdm(AdDTOWrapper wrapper, BidRequest bidRequest, Affiliate affiliate) {
    AdDTO adDTO = wrapper.getAdDTO();
    String impTrackUrls = adDTO.getAdGroup().getImpTrackUrls();

    // 构建展示追踪链
    List<String> impTrackList = new ArrayList<>();
    String systemImpTrack = this.impUrl + AUCTION_PRICE_PARAM;
    String sign = SignHelper.digest(wrapper.getBidId(), adDTO.getCampaign().getId().toString());
    impTrackList.add(SignHelper.urlAddSign(systemImpTrack, sign));
    if (StrUtil.isNotBlank(impTrackUrls)) {
      String[] split = impTrackUrls.split(",");
      impTrackList.addAll(Arrays.asList(split));
    }
    impTrackList = impTrackList.stream()
                               .map(i -> urlFormat(i, sign, wrapper, bidRequest, affiliate))
                               .collect(Collectors.toList());

    // 构建点击追踪链
    String clickTrackUrls = adDTO.getAdGroup().getClickTrackUrls();
    List<String> clickTrackList = new ArrayList<>();
    String systemClickTrack = this.clickUrl;
    clickTrackList.add(SignHelper.urlAddSign(systemClickTrack, sign));
    if (StrUtil.isNotBlank(clickTrackUrls)) {
      String[] split = clickTrackUrls.split(",");
      clickTrackList.addAll(Arrays.asList(split));
    }
    clickTrackList = clickTrackList.stream()
                                   .map(i -> urlFormat(i, sign, wrapper, bidRequest, affiliate))
                                   .collect(Collectors.toList());

    String deepLink =
            urlFormat(adDTO.getAdGroup().getDeeplink(), sign, wrapper, bidRequest, affiliate);
    String forceLink =
            urlFormat(adDTO.getAdGroup().getForceLink(), sign, wrapper, bidRequest, affiliate);

    String clickUrl;
    if (StrUtil.isNotBlank(wrapper.getLandingPage())) {  // 当前流量命中 AE RTA 受众
      String deviceId = bidRequest.getDevice().getIfa();
      clickUrl = AeHelper.landingPageFormat(wrapper.getLandingPage(), wrapper, sign, deviceId, affiliate.getId());
      if (StrUtil.isNotBlank(wrapper.getDeeplink()) && Math.random() * 100 < aeDeeplinkRatio) {
        deepLink = AeHelper.landingPageFormat(wrapper.getDeeplink(), wrapper, sign, deviceId, affiliate.getId());
        wrapper.setUseDeeplink(true);
      } else {
        forceLink = clickUrl;
      }
    } else {
      clickUrl = urlFormat(adDTO.getAdGroup().getClickUrl(), sign, wrapper, bidRequest, affiliate);
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
                          urlFormat(impInfoUrl, sign, wrapper, bidRequest, affiliate),
                          forceLink);
        } else {
          adm = AdmGenerator.bannerAdm(clickUrl,
                  deepLink,
                  adDTO.getCreativeMap().get(adDTO.getAd().getImage()).getUrl(),
                  impTrackList,
                  clickTrackList,
                  urlFormat(impInfoUrl, sign, wrapper, bidRequest, affiliate));
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
        if (buildAdmObject()) {
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

  private String urlFormat(String url,
                           String sign,
                           AdDTOWrapper response,
                           BidRequest bidRequest,
                           Affiliate affiliate) {
    if (url == null) {
      return null;
    }
    if (sign != null) {
      url = url.replace(FormatKey.SIGN, sign);
    }
    url = url.replace(FormatKey.BID_ID, response.getBidId())
             .replace(FormatKey.IMP_ID, response.getImpId())
             .replace(FormatKey.CAMPAIGN_ID,
                      String.valueOf(response.getAdDTO().getCampaign().getId()))
             .replace(FormatKey.AFFILIATE_ID, String.valueOf(affiliate.getId()))
             .replace(FormatKey.AD_GROUP_ID,
                      String.valueOf(response.getAdDTO().getAdGroup().getId()))
             .replace(FormatKey.AD_ID, String.valueOf(response.getAdDTO().getAd().getId()))
             .replace(FormatKey.CREATIVE_ID,
                      String.valueOf(CreativeHelper.getCreativeId(response.getAdDTO().getAd())))
             .replace(FormatKey.DEVICE_ID, bidRequest.getDevice().getIfa())
             .replace(FormatKey.IP, encode(bidRequest.getDevice().getIp()))
             .replace(FormatKey.COUNTRY, bidRequest.getDevice().getGeo().getCountry())
             .replace(FormatKey.OS, bidRequest.getDevice().getOs())
             .replace(FormatKey.DEVICE_MAKE, encode(bidRequest.getDevice().getMake()))
             .replace(FormatKey.DEVICE_MODEL, encode(bidRequest.getDevice().getModel()))
             .replace(FormatKey.AD_FORMAT,
                      AdTypeEnum.of(response.getAdDTO().getAd().getType()).getDesc())
             .replace(FormatKey.BUNDLE, encode(bidRequest.getApp().getBundle()))
             .replace(FormatKey.RTA_TOKEN,
                      encode(StringUtils.firstNonEmpty(response.getRtaToken(), "")));
    return url;
  }

  protected String encode(Object content) {
    if (content == null) {
      return "";
    }
    try {
      return URLEncoder.encode(content.toString(), "utf-8");
    } catch (Exception e) {
      return "";
    }
  }
}
