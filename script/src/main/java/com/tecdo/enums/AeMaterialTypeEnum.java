package com.tecdo.enums;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by Zeki on 2023/4/11
 */
public enum AeMaterialTypeEnum {

    DPA("DPA"),
    STATIC("STATIC"),
    INSTALL("INSTALL"),

    UNKNOWN("UNKNOWN")
    ;

    private final String desc;

    AeMaterialTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static AeMaterialTypeEnum of(String desc) {
        return Arrays.stream(AeMaterialTypeEnum.values())
                .filter(e -> Objects.equals(e.getDesc(), desc)).findFirst()
                .orElse(UNKNOWN);
    }
}
