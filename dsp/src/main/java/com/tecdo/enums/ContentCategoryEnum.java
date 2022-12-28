package com.tecdo.enums;

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

    /**
     * 值
     */
    private final String value;
    /**
     * 描述
     */
    private final String desc;
}
