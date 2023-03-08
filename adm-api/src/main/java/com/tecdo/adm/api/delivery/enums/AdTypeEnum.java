package com.tecdo.adm.api.delivery.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 物料类型
 *
 * Created by Zeki on 2022/12/22
 **/
@Getter
@AllArgsConstructor
public enum AdTypeEnum {

    BANNER(1, "BANNER"),
    VIDEO(2, "VIDEO"),
    AUDIO(3, "AUDIO"),
    NATIVE(4, "NATIVE");

    private final int type;
    private final String desc;

    public static AdTypeEnum of(int type) {
        return Arrays.stream(AdTypeEnum.values()).filter(e -> e.type == type).findFirst().orElse(null);
    }
}
