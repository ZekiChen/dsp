package com.tecdo.domain.biz.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * CTR预估请求 顶层对象模型
 *
 * Created by Zeki on 2023/1/9
 **/
@Setter
@Getter
@Builder
public class CtrRequest implements Serializable {

    /**
     * 用来区分参与预估的广告，不参与模型预估
     */
    private Integer adId;

    /**
     * 日期 yyyy-MM-dd
     */
    private String day;

    /**
     * adx的id
     */
    private Integer affiliateId;

    /**
     * 广告类型
     */
    private String adFormat;

    /**
     * 广告高度
     */
    private Integer adHeight;

    /**
     * 广告宽度
     */
    private Integer adWidth;

    /**
     * Android / IOS / 其他
     */
    private String os;

    /**
     * 设备制造商
     */
    private String deviceMake;

    /**
     * 流量所在的包名
     */
    private String bundle;

    /**
     * 国家三字码
     */
    private String country;

    /**
     * 素材id
     */
    private Integer creativeId;

    /**
     * 广告底价
     */
    private Double bidFloor;

    /**
     * RTA人群特征
     */
    private Integer rtaFeature;

    /**
     * 投放的产品的包名
     */
    private String packageName;

    /**
     * 投放的产品的category
     */
    private String category;

}
