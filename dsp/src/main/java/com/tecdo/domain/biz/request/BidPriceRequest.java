package com.tecdo.domain.biz.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Zeki on 2023/8/18
 */
@Setter
@Getter
@Builder
public class BidPriceRequest implements Serializable {

    private String country;
    private Integer affiliateId;
    private List<BidPriceInfo> bidPriceInfos;
}
