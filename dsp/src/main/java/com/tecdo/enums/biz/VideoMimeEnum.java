package com.tecdo.enums.biz;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 目前我们支持的内容mime-type
 *
 * Created by Zeki on 2023/7/26
 */
@Getter
@AllArgsConstructor
public enum VideoMimeEnum {

    MP4("mp4", "video/mp4"),
    OTHER("other", "other")
    ;

    private final String suffix;
    private final String mime;

    public static VideoMimeEnum of(String suffix) {
        return Arrays.stream(VideoMimeEnum.values()).filter(e -> e.suffix.equals(suffix)).findAny().orElse(OTHER);
    }
}
