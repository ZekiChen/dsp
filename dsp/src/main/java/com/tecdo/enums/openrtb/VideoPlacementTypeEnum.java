package com.tecdo.enums.openrtb;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 视频放置类型
 *
 * Created by Zeki on 2022/12/23
 **/
@Getter
@AllArgsConstructor
public enum VideoPlacementTypeEnum {

    IN_STREAM("1", "In-Stream Played before, during or after the streaming video content that the consumer has requested (e.g., Pre-roll, Mid-roll, Post-roll)."),
    IN_BANNER("2", "In-Banner Exists within a web banner that leverages the banner space to deliver a video experience as opposed to another static or rich media format. The format relies on the existence of display ad inventory on the page for its delivery."),
    IN_ARTICLE("3", "In-Article Loads and plays dynamically between paragraphs of editorial content; existing as a standalone branded message."),
    IN_FEED("4", "In-Feed - Found in content, social, or product feeds."),
    INTERSTITIAL("5", "Interstitial/Slider/Floating Covers the entire or a portion of screen area, but is always on screen while displayed (i.e. cannot be scrolled out of view). Note that a full-screen interstitial (e.g., in mobile) can be distinguished from a floating/slider unit by the imp.instl field."),

    // 非标准协议，用于自定义处理
    OTHER("6", "Other"),
    ;

    private final String value;
    private final String desc;
}
