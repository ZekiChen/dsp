package com.tecdo.service.rta.ae;

/**
 * Created by Zeki on 2023/4/19
 */
public class AeHelper {

    public static String landingPageFormat(String landingPage, String bidId, String sign) {
        return landingPage.replace(AeFormatKey.BID_ID, bidId).replace(AeFormatKey.SIGN, sign);
    }
}
