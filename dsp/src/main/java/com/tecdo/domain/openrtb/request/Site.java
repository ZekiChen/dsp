package com.tecdo.domain.openrtb.request;

import com.tecdo.domain.openrtb.base.Extension;
import com.tecdo.enums.openrtb.ContentCategoryEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 渠道网站对象
 * 即如果广告载体是一个 网站 时应该包含这个对象，如果是 非浏览器 应用时则不需要。
 * 一个竞价请求一定不能同时包含 Site 对象和 App 对象。提供一个站点标识或者页面地址是很有用的，但是不是严格必须的。
 *
 * Created by Zeki on 2022/12/23
 **/
@Setter
@Getter
public class Site extends Extension {

    /**
     * （推荐）
     */
    String id;

    /**
     * 站点名称（可以在展示者请求中作为别名标识）
     */
    private String name;

    /**
     * 站点的域名（例如，“mysite.foo.com”)
     */
    private String domain;

    /**
     * 站点的一组IAB内容类型
     * @see ContentCategoryEnum
     */
    private List<String> cat;

    /**
     * 描述站点当前部分的一组IAB内容类型
     * @see ContentCategoryEnum
     */
    private List<String> sectioncat;

    /**
     * 描述站点当前视图的一组IAB内容类型
     * @see ContentCategoryEnum
     */
    private List<String> pagecat;

    /**
     * 展示广告将要被展示的页面地址
     */
    private String page;

    /**
     * 引导到当前页面的referrer地址
     */
    private String ref;

    /**
     * 引导到当前页面的搜索字符串
     */
    private String search;

    /**
     * 移动优化标志
     * 0-否； 1-是
     */
    private Integer mobile;

    /**
     * 表示该站点是否有隐私策略
     * 0-没有； 1-有
     */
    private Integer privacypolicy;

    /**
     * 站点渠道的详细信息
     */
    private Publisher publisher;

    /**
     * 该站点内容的详细信息
     */
    private Content content;

    /**
     * 逗号分隔的站点的关键字信息
     */
    private String keywords;
}
