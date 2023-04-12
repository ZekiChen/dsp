package com.tecdo.service.rta.ae;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Zeki on 2023/4/4
 */
@Setter
@Getter
public class AeRtaProductInfoVO implements Serializable {

    private String landingPage;
    private String imageUrlList;

    private String productId;
    private String price;
    private String salePrice;
    private String currency;
    private String titleLang;
}
