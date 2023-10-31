package com.tecdo.adm.api.delivery.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Created by Zeki on 2022/12/22
 **/
@Getter
@AllArgsConstructor
public enum ConditionEnum {

    IMP_FREQUENCY("imp_frequency"),
    IMP_FREQUENCY_HOUR("imp_frequency_hour"),
    CLICK_FREQUENCY("click_frequency"),
    CLICK_FREQUENCY_HOUR("click_frequency_hour"),

    AFFILIATE("affiliate"),

    BUNDLE("bundle"),
    AUTO_BUNDLE("auto_bundle"),
    AUTO_BUNDLE_EXCEPT("auto_bundle_except"),

    CATEGORY("category"),
    TAG("tag"),

    CONNECTION_TYPE("connection_type"),
    DEVICE_COUNTRY("device_country"),
    DEVICE_LANG("device_lang"),
    DEVICE_MAKE("device_make"),
    DEVICE_OS("device_os"),
    DEVICE_OSV("device_osv"),
    HOUR("hour"),
    AUDIENCE_AF("audience_af"),
    AFFILIATE_BLOCKED_AD("affiliate_blocked_ad"),

    BANNER_POS("banner_pos"),
    VIDEO_POS("video_pos"),
    IMAGE_INSTL("image_instl"),
    VIDEO_INSTL("video_instl"),
    VIDEO_PLACEMENT("video_placement"),

    BUNDLE_IMP_CAP_DAY("bundle_imp_cap_day"),
    BUNDLE_CLICK_CAP_DAY("bundle_click_cap_day"),
    BUNDLE_COST_CAP_DAY("bundle_cost_cap_day"),

    BUNDLE_BLACK_CLICK("bundle_black_click"),
    BUNDLE_BLACK_IMP("bundle_black_imp"),
    BUNDLE_BLACK_CTR("bundle_black_ctr"),
    BUNDLE_BLACK_ROI("bundle_black_roi"),

    SCHAIN("schain"),
    ;

    private final String desc;

    public static ConditionEnum of(String desc) {
        return Arrays.stream(ConditionEnum.values()).filter(e -> e.desc.equals(desc)).findFirst().orElse(null);
    }
}
