package com.tecdo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API框架
 *
 * Created by Zeki on 2022/12/23
 **/
@Getter
@AllArgsConstructor
public enum APIFrameworkEnum {

    VPAID_1("1", "VPAID 1.0"),
    VPAID_2("2", "VPAID 2.0"),
    MRAID_1("3", "MRAID-1"),
    ORMMA("4", "ORMMA"),
    MRAID_2("5", "MRAID-2"),
    MRAID_3("6", "MRAID-3");

    /**
     * 值
     */
    private final String value;
    /**
     * 描述
     */
    private final String desc;
}
