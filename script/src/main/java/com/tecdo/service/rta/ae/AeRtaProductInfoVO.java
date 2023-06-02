package com.tecdo.service.rta.ae;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Zeki on 2023/4/4
 */
public class AeRtaProductInfoVO implements Serializable {

    @JSONField(name = "product_id")
    private String productId;

    @JSONField(name = "landing_page")
    private String landingPage;

    private String price;

    @JSONField(name = "sale_price")
    private String salePrice;

    private String currency;

    @JSONField(name = "title_lang")
    private String titleLang;

    @JSONField(name = "image_url_list")
    private List<String> imageUrlList;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getLandingPage() {
        return landingPage;
    }

    public void setLandingPage(String landingPage) {
        this.landingPage = landingPage;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(String salePrice) {
        this.salePrice = salePrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTitleLang() {
        return titleLang;
    }

    public void setTitleLang(String titleLang) {
        this.titleLang = titleLang;
    }

    public List<String> getImageUrlList() {
        return imageUrlList;
    }

    public void setImageUrlList(List<String> imageUrlList) {
        this.imageUrlList = imageUrlList;
    }
}
