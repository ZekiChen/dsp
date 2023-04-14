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
    private Integer uvCnt;
    private Integer mbrCnt;
    private Double orderAmount;
    private Integer buyerCnt;
    private Double p4pRevenue;
    private Double affiRevenue;
    private Integer newRegister;

    @JsonProperty("rtaSubId1")
    private String bidId;
    @JsonProperty("rtaSubId2")
    private String sign;
    private String rtaSubId3;
    private String rtaSubId4;
    private String rtaSubId5;

    @JsonProperty("campaignId")
    private String advCampaignId;
}
