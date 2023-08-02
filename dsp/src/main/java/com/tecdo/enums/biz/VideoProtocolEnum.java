package com.tecdo.enums.biz;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 目前我们支持的视频竞价响应协议
 *
 * Created by Zeki on 2023/7/26
 */
@Getter
@AllArgsConstructor
public enum VideoProtocolEnum {

    VAST_3(3, "VAST 3.0"),
    VAST_4(7, "VAST 4.0"),
    OTHER(0, "OTHER"),
    ;

    private final int type;
    private final String desc;

    public static VideoProtocolEnum of(int type) {
        return Arrays.stream(VideoProtocolEnum.values()).filter(e -> type == e.type).findAny().orElse(OTHER);
    }
}
