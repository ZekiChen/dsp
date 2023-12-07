package com.tecdo.util;

import cn.hutool.crypto.SecureUtil;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.common.constant.Constant;
import com.tecdo.constant.FormatKey;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Device;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.util.Optional;

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
        String adId = String.valueOf(response.getAdDTO().getAd().getId());
        url = url.replace(FormatKey.BID_ID, response.getBidId())
                .replace(FormatKey.IMP_ID, response.getImpId())
                .replace(FormatKey.CAMPAIGN_ID,
                        String.valueOf(response.getAdDTO().getCampaign().getId()))
                .replace(FormatKey.AFFILIATE_ID, String.valueOf(affiliate.getId()))
                .replace(FormatKey.AD_GROUP_ID,
                        String.valueOf(response.getAdDTO().getAdGroup().getId()))
                .replace(FormatKey.AD_ID, adId)
                .replace(FormatKey.AD_ID_MD5, SecureUtil.md5().digestHex(adId))
                .replace(FormatKey.AD_ID_SHA256, SecureUtil.sha256().digestHex(adId))
                .replace(FormatKey.CREATIVE_ID,
                        String.valueOf(CreativeHelper.getCreativeId(response.getAdDTO().getAd())))
                .replace(FormatKey.DEVICE_ID, device.getIfa())
                .replace(FormatKey.DEVICE_ID_MD5, SecureUtil.md5().digestHex(device.getIfa()))
                .replace(FormatKey.IP,
                        encode(Optional.ofNullable(device.getIp()).orElse(device.getIpv6())))
                .replace(FormatKey.COUNTRY, device.getGeo().getCountry())
                .replace(FormatKey.OS, device.getOs())
                .replace(FormatKey.DEVICE_MAKE, encode(device.getMake()))
                .replace(FormatKey.DEVICE_MODEL, encode(device.getModel()))
                .replace(FormatKey.AD_FORMAT,
                        AdTypeEnum.of(response.getAdDTO().getAd().getType()).getDesc())
                .replace(FormatKey.BUNDLE, encode(bidRequest.getApp().getBundle()))
                .replace(FormatKey.SCHAIN, encode(ExtHelper.listSChain(bidRequest.getSource())))
                .replace(FormatKey.RTA_TOKEN,
                        encode(StringUtils.firstNonEmpty(response.getRtaToken(), "")));
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
