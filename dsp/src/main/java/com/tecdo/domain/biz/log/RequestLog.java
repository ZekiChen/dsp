package com.tecdo.domain.biz.log;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * BidRequest 参数校验通过后，记录竞价请求日志
 * <p>
 * Created by Zeki on 2023/1/31
 */
@Builder
@Getter
@Setter
public class RequestLog implements Serializable {

    /**
     * 日志产生的时间，精确到小时 yyyy-MM-dd_HH
     */
    @JsonProperty("create_time")
    private String createTime;

    @JsonProperty("request_id")
    private String requestId;

    /**
     * 后13位为unix时间戳
     */
    @JsonProperty("bid_id")
    private String bidId;

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
     * 1-是插屏/全屏广告
     * 0-不是插屏广告
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

    @JsonProperty("city")
    private String city;

    @JsonProperty("region")
    private String region;

    @JsonProperty("device_type")
    private String deviceType;

    @JsonProperty("screen_width")
    private Integer screenWidth;

    @JsonProperty("screen_height")
    private Integer screenHeight;

    @JsonProperty("screen_ppi")
    private Integer screenPpi;

    @JsonProperty("tag_id")
    private String tagId;

    @JsonProperty("rta_request")
    private int rtaRequest;

    @JsonProperty("rta_request_true")
    private int rtaRequestTrue;


    @JsonProperty("category_list")
    private List<String> categoryList;

    @JsonProperty("tag_list")
    private List<String> tagList;

    @JsonProperty("score")
    private String score;

    @JsonProperty("downloads")
    private Long downloads;

    @JsonProperty("reviews")
    private Long reviews;

    @JsonProperty("badv")
    private List<String> bAdv;

    @JsonProperty("bapp")
    private List<String> bApp;

    @JsonProperty("bcat")
    private List<String> bCat;

    @JsonProperty("video_placement")
    private Integer videoPlacement;

    @JsonProperty("is_rewarded")
    private Integer isRewarded;

    @JsonProperty("schain")
    private String schain;

    @JsonProperty("exception_event")
    private String exceptionEvent;
}
