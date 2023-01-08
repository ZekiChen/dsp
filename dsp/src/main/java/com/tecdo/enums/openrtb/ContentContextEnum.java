package com.tecdo.enums.openrtb;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 内容类型（游戏，视频，文本等）
 *
 * Created by Zeki on 2022/12/23
 **/
@Getter
@AllArgsConstructor
public enum ContentContextEnum {

    VIDEO("1", "Video (i.e., video file or stream such as Internet TV broadcasts)"),
    GAME("2", "Game (i.e., an interactive software game)"),
    MUSIC("3", "Music (i.e., audio file or stream such as Internet radio broadcasts)"),
    APPLICATION("4", "Application (i.e., an interactive software application)"),
    TEXT("5", "Text (i.e., primarily textual document such as a web page, eBook, or news article)"),
    OTHER("6", "Other (i.e., none of the other categories applies)"),
    UNKNOWN ("7", "Unknown"),
    ;

    private final String value;
    private final String desc;
}
