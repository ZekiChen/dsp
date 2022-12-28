package com.tecdo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 回放方式
 *
 * Created by Zeki on 2022/12/23
 **/
@Getter
@AllArgsConstructor
public enum PlaybackMethodEnum {

    PAGE_LOAD_SOUND_ON("1", "Initiates on Page Load with Sound On"),
    PAGE_LOAD_SOUND_OFF("2", "Initiates on Page Load with Sound Off by Default"),
    CLICK_SOUND_ON("3", "Initiates on Click with Sound On"),
    MOUSE_OVER_SOUND_ON("4", "Initiates on Mouse-Over with Sound On"),
    VIEWPORT_WITH_SOUND_ON("5", "Initiates on Entering Viewport with Sound On"),
    VIEWPORT_WITH_SOUND_OFF("6", "Initiates on Entering Viewport with Sound Off by Default");

    /**
     * 值
     */
    private final String value;
    /**
     * 描述
     */
    private final String desc;
}
