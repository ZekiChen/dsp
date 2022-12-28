package com.tecdo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 广告位置
 *
 * Created by Zeki on 2022/12/22
 **/
@Getter
@AllArgsConstructor
public enum AdPositionEnum {

    UNKNOWN("0", "Unknown"),
    ABOVE_THE_FOLD("1", "Above the Fold"),
    DEPRECATED("2", "DEPRECATED - May or may not be initially visible depending on screen size/resolution."),
    BELOW_THE_FOLD("3", "Below the Fold"),
    HEADER("4", "Header"),
    FOOTER("5", "Footer"),
    SIDEBAR("6", "Sidebar"),
    FULL_SCREEN("7", "Full Screen");

    /**
     * 值
     */
    private final String value;
    /**
     * 描述
     */
    private final String desc;
}
