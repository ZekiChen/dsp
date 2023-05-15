package com.tecdo.adm.api.delivery.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 广告主名称
 *
 * Created by Zeki on 2023/4/4
 */
@Getter
@AllArgsConstructor
public enum AdvEnum {

    LAZADA("Lazada"),
    AE("AliExpress"),
    ;

    private final String desc;

    public static AdvEnum of(String desc) {
        return Arrays.stream(AdvEnum.values())
                .filter(e -> Objects.equals(e.getDesc(), desc)).findFirst().orElse(null);
    }
}
