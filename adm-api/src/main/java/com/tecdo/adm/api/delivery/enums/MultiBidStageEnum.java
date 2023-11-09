package com.tecdo.adm.api.delivery.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 双阶段出价
 * <p>
 * Created by Zeki on 2023/11/7
 */
@Getter
@AllArgsConstructor
public enum MultiBidStageEnum {

    FIRST(0, "first stage"),
    SECOND(1, "second stage"),
    OTHER(-1, "other");

    private final int type;
    private final String desc;

    public static MultiBidStageEnum of(int type) {
        return Arrays.stream(MultiBidStageEnum.values()).filter(e -> e.type == type).findAny().orElse(OTHER);
    }
}
