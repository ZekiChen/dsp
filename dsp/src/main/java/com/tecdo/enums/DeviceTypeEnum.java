package com.tecdo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备类型
 *
 * Created by Zeki on 2022/12/24
 **/
@Getter
@AllArgsConstructor
public enum DeviceTypeEnum {

    VIDEO("1", "Mobile/Tablet, Version 2.0"),
    GAME("2", "Personal Computer, Version 2.0"),
    MUSIC("3", "Connected TV, Version 2.0"),
    APPLICATION("4", "Phone, New for Version 2.2"),
    TEXT("5", "Tablet, New for Version 2.2"),
    OTHER("6", "Connected Device, New for Version 2.2"),
    UNKNOWN ("7", "Set Top Box, New for Version 2.2"),
    ;

    /**
     * 值
     */
    private final String value;
    /**
     * 描述
     */
    private final String desc;
}
