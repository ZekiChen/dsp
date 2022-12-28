package com.tecdo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ADX可以接受的广告类型，除非受到了渠道网站的限制
 *
 * Created by Zeki on 2022/12/22
 **/
@Getter
@AllArgsConstructor
public enum BannerAdTypeEnum {

    TEXT_MOBILE("1", "XHTML Text Ad (usually mobile)"),
    BANNER_MOBILE ("2", "XHTML Banner Ad. (usually mobile)"),
    JS ("3", "JavaScript Ad; must be valid XHTML (i.e., Script Tags Included)"),
    IFRAME ("4", "iframe");

    /**
     * 值
     */
    private final String value;
    /**
     * 描述
     */
    private final String desc;
}
