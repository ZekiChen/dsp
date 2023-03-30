package com.tecdo.domain.biz.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * CTR预估请求 顶层对象模型
 *
 * Created by Zeki on 2023/1/9
 **/
@Setter
@Getter
@Builder
/**
 * 因为这个对象是okhttps使用的，而okhttps使用的序列化工具为fastjson，所以这里要用fastjson的注解
 */
public class CtrRequest implements Serializable {

    /**
     * 用来区分参与预估的广告，不参与模型预估
     */
    private Integer adId;

    /**
     * 日期 yyyy-MM-dd
     */
    @JSONField(name = "day")
    @JsonProperty("day")
    private String dayOld;

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
     * 系统版本
     */
    private String osv;

    /**
     * 设备制造商
     */
    private String deviceMake;

    /**
     * 流量所在的包名
     */
    private String bundleId;

    @JSONField(name = "bundle")
    @JsonProperty("bundle")
    private String bundleOld;

    /**
     * 国家三字码
     */
    private String country;

    /**
     * 网络链接类型
     */
    private Integer connectionType;

    /**
     * 设备型号
     */
    private String deviceModel;

    /**
     * 运营商
     */
    private String carrier;
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
    private Integer feature1;

    @JSONField(name = "rtaFeature")
    @JsonProperty("rtaFeature")
    private Integer rtaFeatureOld;

    /**
     * 投放的产品的包名
     */
    @JSONField(name = "package")
    @JsonProperty("package")
    private String packageName;

    @JSONField(name = "packageName")
    @JsonProperty("packageName")
    private String packageNameOld;

    /**
     * 投放的产品的category
     */
    private String category;

    private Integer pos;

    private String domain;

    private Integer instl;

    private List<String> cat;

    private String ip;

    private String ua;

    private String lang;

    private String deviceId;


    private List<String> categoryList;

    private List<String> tagList;

    private String score;

    private Long downloads;

    private Long reviews;

}
