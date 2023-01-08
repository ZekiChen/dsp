package com.tecdo.enums.openrtb;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 生产质量
 *
 * Created by Zeki on 2022/12/24
 **/
@Getter
@AllArgsConstructor
public enum ProductionQualityEnum {

    UNKNOWN("0", "Unknown"),
    PROFESSIONALLY_PRODUCED("1", "Professionally Produced"),
    PROSUMER("2", "Prosumer"),
    UGC("3", "User Generated (UGC)");

    private final String value;
    private final String desc;
}
