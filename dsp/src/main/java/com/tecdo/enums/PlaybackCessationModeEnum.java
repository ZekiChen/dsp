package com.tecdo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 回放停止模式
 *
 * Created by Zeki on 2022/12/23
 **/
@Getter
@AllArgsConstructor
public enum PlaybackCessationModeEnum {

    VIDEO_COMPLETION("1", "On Video Completion or when Terminated by User"),
    LEAVING_VIEWPORT("2", "On Leaving Viewport or when Terminated by User"),
    LEAVING_VIEWPORT_CONTINUES_UNTIL_VIDEO_COMPLETION("3", "On Leaving Viewport Continues as a Floating/Slider Unit until Video Completion or when Terminated by User");

    /**
     * 值
     */
    private final String value;
    /**
     * 描述
     */
    private final String desc;
}
