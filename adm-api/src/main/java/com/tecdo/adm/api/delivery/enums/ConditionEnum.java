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
    CLICK_FREQUENCY("click_frequency"),
    AFFILIATE("affiliate"),
    BUNDLE("bundle"),
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
    ;

    private final String desc;

    public static ConditionEnum of(String desc) {
        return Arrays.stream(ConditionEnum.values()).filter(e -> e.desc.equals(desc)).findFirst().orElse(null);
    }
}
