package com.tecdo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ADX可能支持的竞价响应协议
 *
 * Created by Zeki on 2022/12/23
 **/
@Getter
@AllArgsConstructor
public enum ProtocolsEnum {

    VAST_1("1", "VAST 1.0"),
    VAST_2("2", "VAST 2.0"),
    MVAST_3("3", "VAST 3.0"),
    VAST_1_WRAPPER("4", "VAST 1.0 Wrapper"),
    VAST_2_WRAPPER("5", "VAST 2.0 Wrapper"),
    VAST_3_WRAPPER("6", "VAST 3.0 Wrapper"),
    VAST_4("7", "VAST 4.0"),
    VAST_4_WRAPPER("8", "VAST 4.0 Wrapper"),
    DAAST_1("9", "DAAST 1.0"),
    DAAST_1_WRAPPER("10", "DAAST 1.0 Wrapper");

    /**
     * 值
     */
    private final String value;
    /**
     * 描述
     */
    private final String desc;
}
