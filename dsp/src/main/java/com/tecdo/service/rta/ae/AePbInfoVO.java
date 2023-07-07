package com.tecdo.service.rta.ae;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Zeki on 2023/4/6
 */
@Setter
@Getter
public class AePbInfoVO implements Serializable {

    private Boolean isRealtime;
    /**
     * 近实时事件，普通用户访问/会员用户访问，uv，0/1/null
     */
    private Integer uvCnt;
    /**
     *  近实时事件，会员用户浏览，0/1/null
     */
    private Integer mbrCnt;

    // 离线事件
    /**
     * 订单金额
     */
    private Double orderAmount;
    /**
     * 付费事件，0/1/null
     */
    private Integer buyerCnt;
    private Double p4pRevenue;
    private Double affiRevenue;
    private Integer newRegister;

    @JsonProperty("rtaSubId1")
    private String bidId;
    @JsonProperty("rtaSubId2")
    private String sign;
    @JsonProperty("rtaSubId3")
    private String campaignToCreativeId;  // campaignId_adGroupId_adId_creativeId
    @JsonProperty("rtaSubId4")
    private String deviceId;
    @JsonProperty("rtaSubId5")
    private String affiliateId;

    @JsonProperty("campaignId")
    private String advCampaignId;
}
