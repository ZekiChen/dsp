package com.tecdo.enums.openrtb;

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

    private final String value;
    private final String desc;
}
