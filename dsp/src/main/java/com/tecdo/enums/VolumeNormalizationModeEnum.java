package com.tecdo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 体积标准模式
 *
 * Created by Zeki on 2022/12/23
 **/
@Getter
@AllArgsConstructor
public enum VolumeNormalizationModeEnum {

    NONE("0", "None"),
    AVERAGE("1", "Ad Volume Average Normalized to Content"),
    PEAK("2", "Ad Volume Peak Normalized to Content"),
    LOUDNESS("3", "Ad Loudness Normalized to Content"),
    CUSTOM("4", "Custom Volume Normalization");

    /**
     * 值
     */
    private final String value;
    /**
     * 描述
     */
    private final String desc;
}
