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

    ACTIVE(1, "active"),
    PAUSE(2, "pause");

    private final int type;
    private final String desc;

    public static BaseStatusEnum of(int type) {
        return Arrays.stream(BaseStatusEnum.values()).filter(e -> e.type == type).findFirst().orElse(null);
    }
}
