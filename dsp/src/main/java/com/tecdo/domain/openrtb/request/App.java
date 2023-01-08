package com.tecdo.domain.openrtb.request;

import com.tecdo.domain.openrtb.base.Extension;
import com.tecdo.enums.openrtb.ContentCategoryEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 如果广告载体是非浏览器应用（通常是移动设备）时应该包含该对象，网站则不需要包含。
 * 一个竞价请求一定不能同时包含 Site 对象和 App 对象。提供一个App标识或者bundle是很有用的，但是不是严格必须的
 *
 * Created by Zeki on 2022/12/23
 **/
@Setter
@Getter
public class App extends Extension {

    /**
     * 交易特定的应用标识（推荐）
     */
    String id;

    /**
     * 应用名称（可以在展示者请求中作为别名标识）
     */
    private String name;

    /**
     * 应用信息或者包名（例如，com.foo.mygame)；需要是在整个交易过程中唯一的标识
     */
    private String bundle;

    /**
     * 应用的域名（例如，“mygame.foo.com”)
     */
    private String domain;

    /**
     * 应用的商店地址
     */
    private String storeurl;

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
     * 应用版本号
     */
    private String ver;

    /**
     * 表示该应用是否有隐私策略
     * 0-没有； 1-有
     */
    private Integer privacypolicy;

    /**
     * 应用是否需要付费
     * 0-免费； 1-付费
     */
    private Integer paid;

    /**
     * 应用渠道的详细信息
     */
    private Publisher publisher;

    /**
     * 该应用内容的详细信息
     */
    private Content content;

    /**
     * 逗号分隔的应用的关键字信息
     */
    private String keywords;
}
