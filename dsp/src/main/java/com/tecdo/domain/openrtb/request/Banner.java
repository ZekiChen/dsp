package com.tecdo.domain.openrtb.request;

import com.tecdo.domain.openrtb.base.Extension;
import com.tecdo.enums.openrtb.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 横幅广告
 *
 * Created by Zeki on 2022/12/22
 **/
@Setter
@Getter
public class Banner extends Extension {

    /**
     * 允许的横幅尺寸（推荐）
     * 如果没有指定，那么强烈建议使用 h 和 w 属性
     */
    private List<Format> format;

    /**
     * 展示的宽度，以像素为单位
     */
    private Integer w;

    /**
     * 展示的高度，以像素为单位
     */
    private Integer h;

    /**
     * 限制的banner类型
     * @see BannerAdTypeEnum
     */
    private List<Integer> btype;

    /**
     * 限制的物料属性
     * @see CreativeAttributeEnum
     */
    private List<Integer> battr;

    /**
     * 广告在屏幕上的位置
     * @see AdPositionEnum
     */
    private Integer pos = 0;

    /**
     * 支持的内容mime-type。常用的 mime-type 包括 application/x-shockwave-flash, image/jpg 以及 image/gif
     */
    private List<String> mimes;

    /**
     * banner 是在 顶层frame 中而不是 iframe 中
     * 0-不是； 1-是
     */
    private Integer topframe;

    /**
     * banner可以扩展的方向
     * @see ExpandableDirectionEnum
     */
    private List<Integer> expdir;

    /**
     * 本次展示支持的API框架列表。如果一个API没有被显式在列表中指明，则表示不支持
     * @see APIFrameworkEnum
     */
    private List<Integer> api;

    /**
     * 唯一标识。推荐在Ad中包含Banner与Video时候使用。值往往从1开始并依次递增，在依次展示中应当是唯一的
     */
    private String id;

    /**
     * 仅与伴随着 Video 的 Banner 广告有关。渲染模式：
     * 0-并发； 1- end-card
     */
    private Integer vcm;

}
