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
    private Integer impCount;
    private Integer clickCount;
    private Double bidPriceTotal;

    @Override
    public String toString() {
        return bundleId + "," + adGroupId;
    }
}
