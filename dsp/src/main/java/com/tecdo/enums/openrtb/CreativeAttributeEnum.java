package com.tecdo.enums.openrtb;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物料/创意属性。可以描述一个广告被服务或作为其限制
 *
 * Created by Zeki on 2022/12/22
 **/
@Getter
@AllArgsConstructor
public enum CreativeAttributeEnum {

    AUDIO_AUTO_PLAY("1", "Audio Ad (Auto-Play)"),
    AUDIO_USER_INITIATED("2", "Audio Ad (User Initiated)"),
    EXPANDABLE_AUTOMATIC("3", "Expandable (Automatic)"),
    EXPANDABLE_CLICK("4", "Expandable (User Initiated - Click)"),
    EXPANDABLE_ROLLOVER("5", "Expandable (User Initiated - Rollover)"),
    IN_BANNER_VIDEO_AUTO_PLAY("6", "In-Banner Video Ad (Auto-Play)"),
    IN_BANNER_VIDEO_USER_INITIATED("7", "In-Banner Video Ad (User Initiated)"),
    POP("8", "Pop (e.g., Over, Under, or Upon Exit)"),
    PROVOCATIVE("9", "Provocative or Suggestive Imagery"),
    SHAKY("10", "Shaky, Flashing, Flickering, Extreme Animation, Smileys"),
    SURVEYS("11", "Surveys"),
    TEXT_ONLY("12", "Text Only"),
    USER_INTERACTIVE("13", "User Interactive (e.g., Embedded Games)"),
    WINDOWS_DIALOG_OR_ALERT("14", "Windows Dialog or Alert Style"),
    HAS_AUDIO_BUTTON("15", "Has Audio On/Off Button"),
    SKIP_BUTTON("16", "Ad Provides Skip Button (e.g. VPAID-rendered skip button on pre-roll video)"),
    ADOBE_FLASH("17", "Adobe Flash");

    private final String value;
    private final String desc;
}
