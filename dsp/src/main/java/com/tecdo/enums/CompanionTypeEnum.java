package com.tecdo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * VAST companion 广告类型
 *
 * Created by Zeki on 2022/12/23
 **/
@Getter
@AllArgsConstructor
public enum CompanionTypeEnum {

    STATIC("1", "Static Resource"),
    HTML("2", "HTML Resource"),
    IFRAME("3", "iframe Resource");

    /**
     * 值
     */
    private final String value;
    /**
     * 描述
     */
    private final String desc;
}
