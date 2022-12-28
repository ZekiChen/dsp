package com.tecdo.domain.request;

import com.tecdo.domain.base.Extension;
import lombok.Getter;
import lombok.Setter;

/**
 * 从 ADX 上游发出竞价请求的实体的性质和行为
 *
 * Created by Zeki on 2022/12/22
 **/
@Setter
@Getter
public class Source extends Extension {

    /**
     * 实体负责的最终展示售卖决策（推荐）
     * 0-ADX； 1-上游源
     */
    private Integer fd;

    /**
     * 交易ID必须在此竞价请求的所有参与者(例如，潜在的多个ADX)中通用。（推荐）
     */
    private String tid;

    /**
     * 支付ID链字符串，包含TAG支付ID协议v1.0中描述的嵌入式语法（推荐）
     */
    private String pchain;
}



