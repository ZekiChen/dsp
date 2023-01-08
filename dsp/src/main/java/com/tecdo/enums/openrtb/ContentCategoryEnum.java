package com.tecdo.enums.openrtb;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IAB 内容类别
 *
 * Created by Zeki on 2022/12/22
 **/
@Getter
@AllArgsConstructor
public enum ContentCategoryEnum {

    IAB1("IAB1", "Arts & Entertainment"),
    IAB1_1 ("IAB1-1", "Books & Literature"),
    // TODO 太多，暂时跳过
    ;

    private final String value;
    private final String desc;
}
