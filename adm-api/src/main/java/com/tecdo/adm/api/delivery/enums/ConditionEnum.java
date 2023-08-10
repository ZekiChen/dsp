package com.tecdo.adm.api.delivery.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
    ;

    private final String desc;
}
