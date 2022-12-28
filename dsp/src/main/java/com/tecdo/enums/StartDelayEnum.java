package com.tecdo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * video/audio广告 启动延时。 如果开始延迟值大于0，则位置为滚中，该值表示开始延迟
 *
 * Created by Zeki on 2022/12/23
 **/
@Getter
@AllArgsConstructor
public enum StartDelayEnum {

    MID_ROLL("> 0", "Mid-Roll (value indicates start delay in second)"),
    PRE_ROLL("0", "Pre-Roll"),
    GENERIC_MID_ROLL("-1", "Generic Mid-Roll"),
    GENERIC_POST_ROLL("-2", "Generic Post-Roll");

    /**
     * 值
     */
    private final String value;
    /**
     * 描述
     */
    private final String desc;
}
