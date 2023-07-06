package com.tecdo.service.rta.ae;

import com.tecdo.domain.biz.dto.AdDTOWrapper;

/**
 * Created by Zeki on 2023/4/19
 */
public class AeHelper {

    public static String landingPageFormat(String landingPage, AdDTOWrapper wrapper, String sign) {
        return landingPage
                .replace(AeFormatKey.BID_ID, wrapper.getBidId())
                .replace(AeFormatKey.SIGN, sign)
                .replace(AeFormatKey.CAMPAIGN_ID, wrapper.getAdDTO().getCampaign().getId().toString())
                .replace(AeFormatKey.AD_GROUP_ID, wrapper.getAdDTO().getAdGroup().getId().toString());
    }
}
