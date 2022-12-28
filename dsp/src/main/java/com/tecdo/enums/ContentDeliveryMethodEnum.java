package com.tecdo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 内容传输方式
 *
 * Created by Zeki on 2022/12/23
 **/
@Getter
@AllArgsConstructor
public enum ContentDeliveryMethodEnum {

    STREAMING("1", "Streaming"),
    PROGRESSIVE("2", "Progressive"),
    DOWNLOAD("3", "Download");

    /**
     * 值
     */
    private final String value;
    /**
     * 描述
     */
    private final String desc;
}
