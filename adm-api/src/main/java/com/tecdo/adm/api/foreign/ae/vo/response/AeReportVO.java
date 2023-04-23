package com.tecdo.adm.api.foreign.ae.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Zeki on 2023/4/4
 */
@Setter
@Getter
@Builder
public class AeReportVO implements Serializable {

    @JsonProperty("campaign_id")
    private String campaignId;
    private Double cost;
    private Long impressions;
    private Long clicks;
    private Double cpm;
    private Double cpc;
    private Double ctr;

}
