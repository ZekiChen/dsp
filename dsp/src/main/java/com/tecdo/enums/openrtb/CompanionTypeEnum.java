package com.tecdo.enums.openrtb;

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

    STATIC(1, "Static Resource"),
    HTML(2, "HTML Resource"),
    IFRAME(3, "iframe Resource");

    private final Integer value;
    private final String desc;
}
