package com.tecdo.service.rta.ae;

import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.util.CreativeHelper;

import java.util.Objects;

/**
 * Created by Zeki on 2023/4/19
 */
public class AeHelper {

    public static String landingPageFormat(String landingPage, AdDTOWrapper wrapper, String sign,
                                           String deviceId, Integer affiliateId) {
        String campaignId = wrapper.getAdDTO().getCampaign().getId().toString();
        String adGroupId = wrapper.getAdDTO().getAdGroup().getId().toString();
        String adId = wrapper.getAdDTO().getAd().getId().toString();
        String creativeId = Objects.requireNonNull(CreativeHelper.getCreativeId(wrapper.getAdDTO().getAd())).toString();

        return landingPage
                .replace(AeFormatKey.BID_ID, wrapper.getBidId())
                .replace(AeFormatKey.SIGN, sign)
                .replace(AeFormatKey.CAMPAIGN_ADGROUP_AD_CREATIVE, campaignId + "_" + adGroupId + "_" + adId + "_" + creativeId)
                .replace(AeFormatKey.DEVICE_ID, deviceId)
                .replace(AeFormatKey.AFFILIATE_ID, affiliateId.toString());
    }
}
