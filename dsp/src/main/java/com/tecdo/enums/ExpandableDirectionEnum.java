package com.tecdo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 可扩展的方向
 *
 * Created by Zeki on 2022/12/22
 **/
@Getter
@AllArgsConstructor
public enum ExpandableDirectionEnum {

    LEFT("1", "Left"),
    RIGHT("2", "Right"),
    UP("3", "Up"),
    DOWN("4", "Down"),
    FULL_SCREEN("5", "Full Screen");

    /**
     * 值
     */
    private final String value;
    /**
     * 描述
     */
    private final String desc;
}
