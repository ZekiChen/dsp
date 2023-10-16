package com.tecdo.adm.api.doris.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Elwin on 2023/10/11
 */
@Data
public class BundleCost implements Serializable{
    private String bundleId;
    private Integer adGroupId;
    private Long impCount = 0L;
    private Long clickCount = 0L;
    private Double bidPriceTotal = 0D;

    @Override
    public String toString() {
        return bundleId + "," + adGroupId;
    }
}
