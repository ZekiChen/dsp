package com.tecdo.enums.biz;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Created by Zeki on 2023/8/8
 */
@Getter
@AllArgsConstructor
public enum AudienceEncryptEnum {

    NO(0, "no"),
    SHA256(1, "SHA256"),
    OTHER(-1, "other")
    ;

    private final int code;
    private final String value;

    public static AudienceEncryptEnum of(int code) {
        return Arrays.stream(AudienceEncryptEnum.values()).filter(e -> e.code == code).findAny().orElse(OTHER);
    }
}
