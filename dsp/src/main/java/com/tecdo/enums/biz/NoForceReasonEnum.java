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
public enum NoForceReasonEnum {

    PARAM_MISS(1, "parameter missing"),
    WINDOW_VALID(2, "window valid"),
    FUNNEL_VALID(3, "funnel valid"),
    IP_NOT_MATCH(4, "ip not match"),
    OTHER(-1, "OTHER"),
    ;

    private final int code;
    private final String desc;

    public static NoForceReasonEnum of(int type) {
        return Arrays.stream(NoForceReasonEnum.values()).filter(e -> type == e.code).findAny().orElse(OTHER);
    }
}
