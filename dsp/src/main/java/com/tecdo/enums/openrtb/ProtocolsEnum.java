package com.tecdo.enums.openrtb;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ADX可能支持的竞价响应协议
 *
 * VAST（Video Ad Serving Template，视频广告投放模板）：
 * 主要用于在线视频媒体获取视频广告的一种通讯协议，描述了视频广告响应的XML结构。
 *
 * DAAST（Digital Audio Ad Serving Template，数字音频广告投放模板）：
 * 用于在线音频媒体获取音频广告的通讯协议，描述了音频广告响应的XML结构。
 *
 * Created by Zeki on 2022/12/23
 **/
@Getter
@AllArgsConstructor
public enum ProtocolsEnum {

    VAST_1("1", "VAST 1.0"),
    VAST_2("2", "VAST 2.0"),
    VAST_3("3", "VAST 3.0"),
    VAST_1_WRAPPER("4", "VAST 1.0 Wrapper"),
    VAST_2_WRAPPER("5", "VAST 2.0 Wrapper"),
    VAST_3_WRAPPER("6", "VAST 3.0 Wrapper"),
    VAST_4("7", "VAST 4.0"),
    VAST_4_WRAPPER("8", "VAST 4.0 Wrapper"),
    DAAST_1("9", "DAAST 1.0"),
    DAAST_1_WRAPPER("10", "DAAST 1.0 Wrapper");

    private final String value;
    private final String desc;
}
