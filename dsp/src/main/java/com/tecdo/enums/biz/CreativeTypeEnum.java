package com.tecdo.enums.biz;

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

    BANNER(1, "banner"),
    VIDEO(2, "video"),
    AUDIO(3, "audio"),
    NATIVE(4, "native");

    private final int type;
    private final String desc;

    public static CreativeTypeEnum of(int type) {
        return Arrays.stream(CreativeTypeEnum.values()).filter(e -> e.type == type).findFirst().orElse(null);
    }
}
