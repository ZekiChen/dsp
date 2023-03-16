package com.tecdo.starter.oss.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Zeki on 2023/3/10
 */
@Setter
@Getter
public class PacFile {

    /**
     * 文件地址（走CDN）
     */
    private String url;

    /**
     * 存桶里的路径，如 upload/20230315/70fdc9d57918c52c9ee76c26a4af4b00.png
     */
    private String name;

    /**
     * 原始文件名，如 测试.png
     */
    private String originalName;
}
