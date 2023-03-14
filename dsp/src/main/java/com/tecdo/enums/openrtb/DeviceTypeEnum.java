package com.tecdo.enums.openrtb;

import java.util.Arrays;

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

    MOBILE(1, "Mobile/Tablet, Version 2.0"),
    PC(2, "Personal Computer, Version 2.0"),
    TV(3, "Connected TV, Version 2.0"),
    PHONE(4, "Phone, New for Version 2.2"),
    TABLET(5, "Tablet, New for Version 2.2"),
    DEVICE(6, "Connected Device, New for Version 2.2"),
    TOPBOX (7, "Set Top Box, New for Version 2.2"),
    UNKNOWN(0,"unknown")
    ;

    private final int type;
    private final String desc;

    public static DeviceTypeEnum of(int type) {
        return Arrays.stream(DeviceTypeEnum.values()).filter(e -> e.type == type).findFirst().orElse(UNKNOWN);
    }
}
