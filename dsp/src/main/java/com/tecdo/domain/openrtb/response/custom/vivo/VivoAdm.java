package com.tecdo.domain.openrtb.response.custom.vivo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * VIVO自定义协议
 * 物料信息
 *
 * Created by Zeki on 2023/9/14
 */
@Setter
@Getter
public class VivoAdm implements Serializable {

    /**
     * 必填
     * 物料格式id：目前只有1000
     */
    private Integer formatId = 1000;
    /**
     * 必填
     * 物料支持的推广形式：1-应用下载，目前仅1
     */
    private List<Integer> promotionTypes = Collections.singletonList(1);

    /**
     * 推广的应用包名
     */
    private String appPackage;
}
