package com.tecdo.job.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Created by Zeki on 2023/7/26
 */
@Getter
@AllArgsConstructor
public enum IdentityEnum {

    MS(1, "Mobisummer"),
    LIQUID(2, "Liquid"),
    ;

    private final int type;
    private final String desc;

    public static IdentityEnum of(int type) {
        return Arrays.stream(IdentityEnum.values()).filter(e -> e.type == type).findFirst().orElse(null);
    }
}