package com.tecdo.adm.api.delivery.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 广告主类型
 *
 * Created by Zeki on 2023/5/11
 */
@Getter
@AllArgsConstructor
public enum AdvTypeEnum {

    NORMAL(1, "Normal"),
    LAZADA_RTA(2, "Lazada RTA"),
    AE_RTA(3, "AliExpress RTA"),
    ;

    private final int type;
    private final String desc;

    public static AdvTypeEnum of(int type) {
        return Arrays.stream(AdvTypeEnum.values()).filter(e -> e.type == type).findFirst().orElse(null);
    }
}
