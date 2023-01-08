package com.tecdo.enums.openrtb;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 网络连接类型
 *
 * Created by Zeki on 2022/12/24
 **/
@Getter
@AllArgsConstructor
public enum ConnectionTypeEnum {

    Unknown("0", "Unknown"),
    Ethernet("1", "Ethernet"),
    WIFI("2", "WIFI"),
    Unknown_Generation("3", "Cellular Network – Unknown Generation"),
    FIRST_G("4", "Cellular Network – 2G"),
    SECOND_G("5", "Cellular Network – 3G"),
    THIRD_G("6", "Cellular Network – 4G");

    private final String value;
    private final String desc;
}
