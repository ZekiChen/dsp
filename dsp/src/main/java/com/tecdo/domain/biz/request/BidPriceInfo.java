package com.tecdo.domain.biz.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Zeki on 2023/8/18
 */
@Setter
@Getter
@Builder
public class BidPriceInfo implements Serializable {

    private Double pctr;
    private Integer adId;
    private Integer adGroupId;
}
