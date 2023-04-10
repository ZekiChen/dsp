package com.tecdo.transform;

import com.tecdo.adm.api.delivery.entity.Affiliate;
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
import com.tecdo.util.AdmGenerator;
import com.tecdo.util.CreativeHelper;
import com.tecdo.util.JsonHelper;
import com.tecdo.util.SignHelper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.hutool.extra.spring.SpringUtil;

@Component
public abstract class AbstractTransform implements IProtoTransform {

  private final String winUrl = SpringUtil.getProperty("pac.notice.win-url");
  private final String impUrl = SpringUtil.getProperty("pac.notice.imp-url");
  private final String clickUrl = SpringUtil.getProperty("pac.notice.click-url");
  private final String AUCTION_PRICE_PARAM = "&bid_success_price=${AUCTION_PRICE}";

  public abstract String deepLinkFormat(String deepLink);

  @Override
  public BidRequest requestTransform(String req) {
    BidRequest bidRequest = JsonHelper.parseObject(req, BidRequest.class);
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

  public BidResponse responseTransform(AdDTOWrapper wrapper, BidRequest bidRequest, Affiliate affiliate) {
    AdDTO adDTO = wrapper.getAdDTO();
    String bidId = wrapper.getBidId();
    BidResponse bidResponse = new BidResponse();
    bidResponse.setId(bidRequest.getId());
    bidResponse.setBidid(bidId);
    Bid bid = new Bid();
    bid.setId(bidId);
    bid.setImpid(wrapper.getImpId());
    bid.setPrice(wrapper.getBidPrice().floatValue());
    String sign = SignHelper.digest(bidId, adDTO.getCampaign().getId().toString());
    String winUrl =
      urlFormat(this.winUrl, sign, wrapper, bidRequest, affiliate) + AUCTION_PRICE_PARAM;
    bid.setNurl(SignHelper.urlAddSign(winUrl, sign));
    bid.setAdm(buildAdm(wrapper, bidRequest, affiliate));
    bid.setAdid(String.valueOf(adDTO.getAd().getId()));
    bid.setAdomain(Collections.singletonList(adDTO.getCampaign().getDomain()));
    bid.setBundle(adDTO.getCampaign().getPackageName());
    bid.setIurl(adDTO.getCreativeMap().get(CreativeHelper.getCreativeId(adDTO.getAd())).getUrl());
    bid.setCid(String.valueOf(adDTO.getCampaign().getId()));
    bid.setCrid(String.valueOf(CreativeHelper.getCreativeId(adDTO.getAd())));

    SeatBid seatBid = new SeatBid();
    seatBid.setBid(Collections.singletonList(bid));
    bidResponse.setSeatbid(Collections.singletonList(seatBid));
    return bidResponse;

  }

  private String buildAdm(AdDTOWrapper wrapper, BidRequest bidRequest, Affiliate affiliate) {
    AdDTO adDTO = wrapper.getAdDTO();
    String adm = null;
    String impTrackUrls = adDTO.getAdGroup().getImpTrackUrls();
    List<String> impTrackList = new ArrayList<>();
    String systemImpTrack = this.impUrl + AUCTION_PRICE_PARAM;
    String sign = SignHelper.digest(wrapper.getBidId(), adDTO.getCampaign().getId().toString());
    impTrackList.add(SignHelper.urlAddSign(systemImpTrack, sign));
    if (impTrackUrls != null) {
      String[] split = impTrackUrls.split(",");
      impTrackList.addAll(Arrays.asList(split));
    }
    impTrackList = impTrackList.stream()
                               .map(i -> urlFormat(i, sign, wrapper, bidRequest, affiliate))
                               .collect(Collectors.toList());

    String clickTrackUrls = adDTO.getAdGroup().getClickTrackUrls();
    List<String> clickTrackList = new ArrayList<>();
    String systemClickTrack = this.clickUrl;
    clickTrackList.add(SignHelper.urlAddSign(systemClickTrack, sign));
    if (clickTrackUrls != null) {
      String[] split = clickTrackUrls.split(",");
      clickTrackList.addAll(Arrays.asList(split));
    }
    clickTrackList = clickTrackList.stream()
                                   .map(i -> urlFormat(i, sign, wrapper, bidRequest, affiliate))
                                   .collect(Collectors.toList());


    String clickUrl =
      urlFormat(adDTO.getAdGroup().getClickUrl(), sign, wrapper, bidRequest, affiliate);
    String deepLink =
      urlFormat(adDTO.getAdGroup().getDeeplink(), sign, wrapper, bidRequest, affiliate);
    deepLink = deepLinkFormat(deepLink);
    if (Objects.equals(adDTO.getAd().getType(), AdTypeEnum.BANNER.getType())) {
      adm = AdmGenerator.bannerAdm(clickUrl,
                                   deepLink,
                                   adDTO.getCreativeMap().get(adDTO.getAd().getImage()).getUrl(),
                                   impTrackList,
                                   clickTrackList);
    }
    if (Objects.equals(adDTO.getAd().getType(), AdTypeEnum.NATIVE.getType())) {
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
      if (imp.getNative1().getNativeRequestWrapper() != null) {
        NativeResponseWrapper nativeResponseWrapper = new NativeResponseWrapper();
        nativeResponseWrapper.setNativeResponse(nativeResponse);
        adm = JsonHelper.toJSONString(nativeResponseWrapper);
      } else {
        adm = JsonHelper.toJSONString(nativeResponse);
      }
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
