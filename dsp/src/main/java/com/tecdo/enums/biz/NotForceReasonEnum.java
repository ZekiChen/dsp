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
public enum NotForceReasonEnum {

    SUCCESS(0, "success"),
    PARAM_MISS(1, "parameter missing"),
    WINDOW_VALID(2, "window valid"),
    DUPLICATE_VALID(3, "duplicate valid"),
    IP_NOT_MATCH(4, "ip not match"),
    OTHER(-1, "OTHER"),
    ;

    private final int code;
    private final String desc;

    public static NotForceReasonEnum of(int type) {
        return Arrays.stream(NotForceReasonEnum.values()).filter(e -> type == e.code).findAny().orElse(OTHER);
    }
}
