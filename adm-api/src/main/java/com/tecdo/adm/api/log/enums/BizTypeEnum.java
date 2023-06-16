package com.tecdo.adm.api.log.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Created by Zeki on 2023/6/14
 */
@Getter
@AllArgsConstructor
public enum BizTypeEnum {

    CAMPAIGN(1, "Campaign"),
    AD_GROUP(2, "AdGroup"),
    AD(3, "Ad");

    private final Integer type;
    private final String desc;

    public static BizTypeEnum of(int type) {
        return Arrays.stream(BizTypeEnum.values()).filter(e -> e.type == type).findFirst().orElse(null);
    }
}
