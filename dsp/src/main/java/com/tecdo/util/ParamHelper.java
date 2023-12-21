package com.tecdo.util;

import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.adm.api.delivery.enums.AdTypeEnumForPixalate;
import com.tecdo.common.constant.Constant;
import com.tecdo.constant.FormatKey;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Content;
import com.tecdo.domain.openrtb.request.Device;
import com.tecdo.domain.openrtb.request.Publisher;
import com.tecdo.domain.openrtb.request.Regs;
import com.tecdo.domain.openrtb.request.Site;
import com.tecdo.domain.openrtb.request.User;

import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import cn.hutool.crypto.SecureUtil;

/**
 * Created by Zeki on 2023/9/14
 */
public class ParamHelper {

  public static String urlFormat(String url,
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
    Device device = bidRequest.getDevice();
    AdDTO adDTO = response.getAdDTO();
    String adId = String.valueOf(adDTO.getAd().getId());
    Integer creativeId = CreativeHelper.getCreativeId(adDTO.getAd());
    Creative creative = adDTO.getCreativeMap().get(creativeId);

    Map<String, Object> replaceMap = new HashMap<>();
    replaceMap.put(FormatKey.BID_ID, response.getBidId());
    replaceMap.put(FormatKey.IMP_ID, response.getImpId());
    replaceMap.put(FormatKey.CAMPAIGN_ID, adDTO.getCampaign().getId());
    replaceMap.put(FormatKey.AFFILIATE_ID, affiliate.getId());
    replaceMap.put(FormatKey.AD_GROUP_ID, adDTO.getAdGroup().getId());
    replaceMap.put(FormatKey.AD_ID, adId);
    replaceMap.put(FormatKey.AD_ID_MD5, SecureUtil.md5().digestHex(adId));
    replaceMap.put(FormatKey.AD_ID_SHA256, SecureUtil.sha256().digestHex(adId));
    replaceMap.put(FormatKey.CREATIVE_ID, creativeId);
    replaceMap.put(FormatKey.DEVICE_ID, device.getIfa());
    replaceMap.put(FormatKey.DEVICE_ID_MD5, SecureUtil.md5().digestHex(device.getIfa()));
    replaceMap.put(FormatKey.IP, Optional.ofNullable(device.getIp()).orElse(device.getIpv6()));
    replaceMap.put(FormatKey.COUNTRY, device.getGeo().getCountry());
    replaceMap.put(FormatKey.OS, device.getOs());
    replaceMap.put(FormatKey.DEVICE_MAKE, device.getMake());
    replaceMap.put(FormatKey.DEVICE_MODEL, device.getModel());
    replaceMap.put(FormatKey.AD_FORMAT, AdTypeEnum.of(adDTO.getAd().getType()).getDesc());
    replaceMap.put(FormatKey.BUNDLE, bidRequest.getApp().getBundle());
    replaceMap.put(FormatKey.SCHAIN, ExtHelper.listSChain(bidRequest.getSource()));
    replaceMap.put(FormatKey.RTA_TOKEN, StringUtils.firstNonEmpty(response.getRtaToken(), ""));
    replaceMap.put(FormatKey.ADV_ID, adDTO.getAdv().getId());
    replaceMap.put(FormatKey.PUBLISH_ID,
                   Optional.ofNullable(bidRequest.getApp().getPublisher())
                           .map(Publisher::getId)
                           .orElse(""));
    replaceMap.put(FormatKey.SITE_ID,
                   Optional.ofNullable(bidRequest.getSite()).map(Site::getId).orElse(""));
    replaceMap.put(FormatKey.BID_PRICE, response.getBidPrice());
    replaceMap.put(FormatKey.CREATIVE_SIZE, creative.getWidth() + "_" + creative.getHeight());
    replaceMap.put(FormatKey.SITE_PAGE,
                   Optional.ofNullable(bidRequest.getSite()).map(Site::getPage).orElse(""));
    replaceMap.put(FormatKey.USER_ID,
                   Optional.ofNullable(bidRequest.getUser()).map(User::getId).orElse(""));
    replaceMap.put(FormatKey.MCCMNC, device.getMccmnc());
    replaceMap.put(FormatKey.CONTENT_ID,
                   Optional.ofNullable(bidRequest.getApp().getContent())
                           .map(Content::getId)
                           .orElse(""));
    replaceMap.put(FormatKey.LAT, device.getGeo().getLat());
    replaceMap.put(FormatKey.LON, device.getGeo().getLon());
    replaceMap.put(FormatKey.CARRIER, device.getCarrier());
    replaceMap.put(FormatKey.AD_FORMAT_2,
                   AdTypeEnumForPixalate.parse(adDTO.getAd().getType()).getDesc());
    replaceMap.put(FormatKey.APP_NAME, bidRequest.getApp().getName());
    replaceMap.put(FormatKey.UA, device.getUa());
    replaceMap.put(FormatKey.SUPPLY_CHAIN, ExtHelper.listSChainForPixalate(bidRequest.getSource()));
    replaceMap.put(FormatKey.APP_VERSION, bidRequest.getApp().getVer());
    replaceMap.put(FormatKey.AFFILIATE_TYPE, "1");
    replaceMap.put(FormatKey.COPPA,
                   Optional.ofNullable(bidRequest.getRegs()).map(Regs::getCoppa).orElse(0));
    replaceMap.put(FormatKey.RANDOM, response.getBidId());
    replaceMap.put(FormatKey.PIXALATE_PLATFORM_ID, "");
    replaceMap.put(FormatKey.PIXALATE_CLIENT_ID, "");

    for (Map.Entry<String, Object> entry : replaceMap.entrySet()) {
      String k = entry.getKey();
      Object v = entry.getValue();
      url = url.replace(k, encode(v));
    }
    return url;
  }

  public static String encode(Object content) {
    if (content == null) {
      return "";
    }
    try {
      return URLEncoder.encode(content.toString(), "utf-8");
    } catch (Exception e) {
      return "";
    }
  }

  public static String getUriParamAsString(String uri) {
    int index = uri.indexOf(Constant.QUESTION_MARK);
    String content = "";
    if (0 < index) {
      content = uri.substring(index + 1);
    }
    return content;
  }
}
