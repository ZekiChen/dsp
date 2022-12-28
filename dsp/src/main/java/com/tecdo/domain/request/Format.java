package com.tecdo.domain.request;

import com.tecdo.domain.base.Extension;
import lombok.Getter;
import lombok.Setter;

/**
 * 该对象表示横幅展示允许的大小(即高度和宽度的组合)或Flex Ad参数。它们通常用于允许多种大小的数组中。
 * 建议指定 w/h对 或 wratio/hratio/wmin集 (即对于Flex Ads)。
 *
 * Created by Zeki on 2022/12/23
 **/
@Setter
@Getter
public class Format extends Extension {

    /**
     * 以设备独立像素(DIPS)为单位的宽度
     */
    private Integer w;

    /**
     * 以设备独立像素(DIPS)为单位的高度
     */
    private Integer h;

    /**
     * 以比率表示尺寸时的相对宽度
     */
    private Integer wratio;

    /**
     * 以比例表示尺寸时的相对高度
     */
    private Integer hratio;

    /**
     * 以设备无关像素(DIPS)为单位显示广告的最小宽度，其大小以比率表示
     */
    private Integer wmin;
}
