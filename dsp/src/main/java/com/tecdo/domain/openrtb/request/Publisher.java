package com.tecdo.domain.openrtb.request;

import com.tecdo.domain.openrtb.base.Extension;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 渠道
 *
 * Created by Zeki on 2022/12/23
 **/
@Setter
@Getter
public class Publisher extends Extension {

    /**
     * 交易特定的渠道标识
     */
    private String id;

    /**
     * 渠道名称（可以在展示者请求中作为别名标识）
     */
    private String name;

    /**
     * 发布者的IAB内容类型数组
     */
    private List<String> cat;

    /**
     * 发布者的顶级域名（例如，“publisher.com”)
     */
    private String domain;
}
