package com.tecdo.domain.request;

import com.tecdo.domain.base.Extension;
import com.tecdo.enums.ContentCategoryEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 定义内容的提供者， 广告会在这些内容中展示。
 * 当内容会被多个渠道展示时是对于区分发布者和生产者是否是同一实体是有用的。
 *
 * Created by Zeki on 2022/12/23
 **/
@Setter
@Getter
public class Producer extends Extension {

    /**
     * 内容生产者标识，当内容会被多个渠道展示且可能使用嵌套标签展示在一个站点的时候有用
     */
    private String id;

    /**
     * 内容提供者名称（例如，“Warner Bros"）
     */
    private String name;

    /**
     * 内容提供者的IAB内容类型数组
     * @see ContentCategoryEnum
     */
    private List<String> cat;

    /**
     * 内容提供者的顶级域名（例如，“producer.com” )
     */
    private String domain;
}
