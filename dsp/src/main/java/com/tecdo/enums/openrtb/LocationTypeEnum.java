package com.tecdo.enums.openrtb;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 位置类型
 *
 * Created by Zeki on 2022/12/24
 **/
@Getter
@AllArgsConstructor
public enum LocationTypeEnum {

    GPS("1", "GPS/Location Services"),
    IP("2", "IP Address"),
    USER_PROVIDED("3", "User provided (e.g., registration data)");

    private final String value;
    private final String desc;
}
