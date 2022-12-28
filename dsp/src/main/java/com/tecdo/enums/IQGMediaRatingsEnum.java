package com.tecdo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 下表列出了基于IQG 2.1分类描述内容时使用的媒体评级
 *
 * Created by Zeki on 2022/12/23
 **/
@Getter
@AllArgsConstructor
public enum IQGMediaRatingsEnum {

    UNKNOWN("1", "All Audiences"),
    ABOVE_THE_FOLD("2", "Everyone Over 12"),
    DEPRECATED("3", "Mature Audiences");

    /**
     * 值
     */
    private final String value;
    /**
     * 描述
     */
    private final String desc;
}
