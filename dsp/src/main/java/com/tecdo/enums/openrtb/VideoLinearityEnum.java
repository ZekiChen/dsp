package com.tecdo.enums.openrtb;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 视频线性度
 *
 * Created by Zeki on 2022/12/23
 **/
@Getter
@AllArgsConstructor
public enum VideoLinearityEnum {

    LINEAR("1", "Linear / In-Stream"),
    NON_LINEAR("2", "Non-Linear / Overlay");

    private final String value;
    private final String desc;
}
