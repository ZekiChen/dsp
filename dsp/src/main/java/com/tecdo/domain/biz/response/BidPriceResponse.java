package com.tecdo.domain.biz.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Zeki on 2023/8/18
 */
@Getter
@Setter
public class BidPriceResponse implements Serializable {

    private Integer adId;
    private Double bidPrice;
}
