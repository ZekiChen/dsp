package com.tecdo.domain.foreign.flatads;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Zeki on 2023/2/24
 */
@Setter
@Getter
public class FlatAdsReportVO implements Serializable {

    /**
     * date
     */
    @JsonProperty("DT")
    private String dt;

    /**
     * eCPM
     */
    @JsonProperty("GROSS_ECPM")
    private Double grossECpm;

    /**
     * revenue amount
     */
    @JsonProperty("GROSS_REVENUE")
    private Double grossRevenue;

    /**
     * impression count
     */
    @JsonProperty("IMPRESSION")
    private Long impression;
}
