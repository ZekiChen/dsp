package com.tecdo.enums.biz;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 放置类型
 *
 * Created by Zeki on 2023/9/12
 */
@Getter
@AllArgsConstructor
public enum PlacementTypeEnum {

    REWARDED(1, "rewarded"),
    OTHER(-1, "OTHER"),
    ;

    private final int type;
    private final String desc;

    public static PlacementTypeEnum of(int type) {
        return Arrays.stream(PlacementTypeEnum.values()).filter(e -> type == e.type).findAny().orElse(OTHER);
    }
}
