package com.tecdo.domain.biz.log;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 成功 BidResponse 才会记录的响应日志
 * <p>
 * Created by Zeki on 2023/1/31
 */
@Builder
@Getter
@Setter
public class ResponseLog implements Serializable {

    /**
     * 日志产生的时间，精确到小时 yyyy-MM-dd_HH
     */
    @JsonProperty("create_time")
    private String createTime;

    @JsonProperty("bid_id")
    private String bidId;

    @JsonProperty("campaign_id")
    private Integer campaignId;
    @JsonProperty("campaign_name")
    private String campaignName;

    @JsonProperty("ad_group_id")
    private Integer adGroupId;
    @JsonProperty("ad_group_name")
    private String adGroupName;

    @JsonProperty("ad_id")
    private Integer adId;
    @JsonProperty("ad_name")
    private String adName;

    @JsonProperty("creative_id")
    private Integer creativeId;
    @JsonProperty("creative_name")
    private String creativeName;

    @JsonProperty("package")
    private String packageName;

    @JsonProperty("category")
    private String category;

    @JsonProperty("feature_1")
    private Integer feature;

    /**
     * 竞价价格
     */
    @JsonProperty("bid_price")
    private Double bidPrice;

    /**
     * 预估ctr
     */
    @JsonProperty("p_ctr")
    private Double pCtr;

    /**
     * 英语预估ctr的模型版本
     */
    @JsonProperty("p_ctr_version")
    private String pCtrVersion;


    // field from request

    /**
     * 交易所id，也是渠道id
     */
    @JsonProperty("affiliate_id")
    private Integer affiliateId;

    /**
     * 交易所名字
     */
    @JsonProperty("affiliate_name")
    private String affiliateName;

    /**
     * 流量类型，banner，native，video
     */
    @JsonProperty("ad_format")
    private String adFormat;

    /**
     * 版位宽度
     */
    @JsonProperty("ad_width")
    private String adWidth;

    /**
     * 版位高度
     */
    @JsonProperty("ad_height")
    private String adHeight;

    /**
     * Android/IOS
     */
    @JsonProperty("os")
    private String os;

    /**
     * 设备厂商
     */
    @JsonProperty("device_make")
    private String deviceMake;

    /**
     * 流量来源媒体
     */
    @JsonProperty("bundle_id")
    private String bundleId;

    /**
     * 国家
     */
    @JsonProperty("country")
    private String country;

    /**
     * 网络连接类型
     */
    @JsonProperty("connection_type")
    private Integer connectionType;

    /**
     * 设备型号
     */
    @JsonProperty("device_model")
    private String deviceModel;

    /**
     * 系统版本
     */
    @JsonProperty("osv")
    private String osv;

    /**
     * 运营商
     */
    @JsonProperty("carrier")
    private String carrier;

    /**
     * 广告所处位置
     */
    @JsonProperty("pos")
    private Integer pos;

    /**
     * 广告是否为插屏/全屏
     */
    @JsonProperty("instl")
    private Integer instl;

    /**
     * 当前bundle的域名
     */
    @JsonProperty("domain")
    private String domain;

    /**
     * 当前bundle所属类别
     */
    @JsonProperty("cat")
    private List<String> cat;

    /**
     * 设备ip
     */
    @JsonProperty("ip")
    private String ip;

    /**
     * 设备ua
     */
    @JsonProperty("ua")
    private String ua;

    /**
     * 设备语言
     */
    @JsonProperty("lang")
    private String lang;

    /**
     * 设备id
     */
    @JsonProperty("device_id")
    private String deviceId;

    /**
     * 竞价底价
     */
    @JsonProperty("bid_floor")
    private Double bidFloor;
}
