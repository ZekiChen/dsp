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
public enum CreativeTypeEnum {

    IMAGE(1, "image"),
    ICON(2, "icon"),
    VIDEO(3, "video");
    private final int type;
    private final String desc;

    public static CreativeTypeEnum of(int type) {
        return Arrays.stream(CreativeTypeEnum.values()).filter(e -> e.type == type).findFirst().orElse(null);
    }
}
