package com.tecdo.domain.biz.log;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tecdo.adm.api.delivery.enums.BidAlgorithmEnum;
import com.tecdo.transform.ResponseTypeEnum;
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

    @JsonProperty("adv_id")
    private Integer advId;
    @JsonProperty("adv_name")
    private String advName;

    @JsonProperty("bid_strategy")
    private Integer bidStrategy;

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
     * 预估ctr的模型版本
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

    /**
     * 素材宽度
     */
    @JsonProperty("creative_width")
    private String creativeWidth;

    /**
     * 素材高度
     */
    @JsonProperty("creative_height")
    private String creativeHeight;

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

    @JsonProperty("rta_token")
    private String rtaToken;

    /**
     * 预估cvr
     */
    @JsonProperty("p_cvr")
    private Double pCvr;

    /**
     * 预估cvr的模型版本
     */
    @JsonProperty("p_cvr_version")
    private String pCvrVersion;

    /**
     * 落地页，目前AE RTA会用到
     */
    @JsonProperty("landing_page")
    private String landingPage;
    @JsonProperty("use_deeplink")
    private Integer useDeeplink;

    @JsonProperty("badv")
    private List<String> bAdv;

    @JsonProperty("bapp")
    private List<String> bApp;

    @JsonProperty("bcat")
    private List<String> bCat;

    /**
     * 竞价响应类型 {@link ResponseTypeEnum}
     */
    @JsonProperty("response_type")
    private Integer responseType;

    /**
     * 出价算法 {@link BidAlgorithmEnum}
     */
    @JsonProperty("bid_algorithm")
    private String bidAlgorithm;

    @JsonProperty("video_placement")
    private Integer videoPlacement;

    @JsonProperty("is_rewarded")
    private Integer isRewarded;

    @JsonProperty("schain")
    private String schain;

    @JsonProperty("imp_frequency")
    private Integer impFrequency;
    @JsonProperty("click_frequency")
    private Integer clickFrequency;

    @JsonProperty("bid_stage")
    private Integer bidStage;
}
