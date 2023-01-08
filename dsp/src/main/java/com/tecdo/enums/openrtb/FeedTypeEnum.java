package com.tecdo.enums.openrtb;

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

    private final String value;
    private final String desc;
}
