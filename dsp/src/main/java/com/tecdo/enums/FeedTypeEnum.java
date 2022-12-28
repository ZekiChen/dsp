package com.tecdo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 音频饲料类型
 *
 * Created by Zeki on 2022/12/23
 **/
@Getter
@AllArgsConstructor
public enum FeedTypeEnum {

    MUSIC("1", "Music Service"),
    FM_OR_AM_BROADCAST("2", "FM/AM Broadcast"),
    PODCAST("3", "Podcast");

    /**
     * 值
     */
    private final String value;
    /**
     * 描述
     */
    private final String desc;
}
