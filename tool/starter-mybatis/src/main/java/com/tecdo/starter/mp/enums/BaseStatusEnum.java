package com.tecdo.starter.mp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Created by Zeki on 2023/3/6
 */
@Getter
@AllArgsConstructor
public enum BaseStatusEnum {

    PAUSE(0, "pause"),
    ACTIVE(1, "active"),
    DELETE(2, "delete"),
    ;

    private final int type;
    private final String desc;

    public static BaseStatusEnum of(int type) {
        return Arrays.stream(BaseStatusEnum.values()).filter(e -> e.type == type).findFirst().orElse(null);
    }
}
