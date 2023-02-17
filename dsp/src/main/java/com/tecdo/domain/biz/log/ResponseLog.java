package com.tecdo.domain.biz.log;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 成功 BidResponse 才会记录的响应日志
 * <p>
 * Created by Zeki on 2023/1/31
 */
@Builder
@Getter
@Setter
public class ResponseLog implements Serializable {

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
}
