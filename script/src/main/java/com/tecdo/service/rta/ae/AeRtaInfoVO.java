package com.tecdo.service.rta.ae;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Zeki on 2023/4/4
 */
//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class AeRtaInfoVO implements Serializable {

    @JSONField(name = "campaign_id")
    private String advCampaignId;

    private Boolean target;

    @JSONField(name = "material_type")
    private String materialType;  // DPA、STATIC、INSTALL

    @JSONField(name = "landing_page")
    private String landingPage;

    @JSONField(name = "deep_link")
    private String deeplink;

    @JSONField(name = "product_list")
    private List<AeRtaProductInfoVO> productList;

    public String getAdvCampaignId() {
        return advCampaignId;
    }

    public void setAdvCampaignId(String advCampaignId) {
        this.advCampaignId = advCampaignId;
    }

    public Boolean getTarget() {
        return target;
    }

    public void setTarget(Boolean target) {
        this.target = target;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public String getLandingPage() {
        return landingPage;
    }

    public void setLandingPage(String landingPage) {
        this.landingPage = landingPage;
    }

    public String getDeeplink() {
        return deeplink;
    }

    public void setDeeplink(String deeplink) {
        this.deeplink = deeplink;
    }

    public List<AeRtaProductInfoVO> getProductList() {
        return productList;
    }

    public void setProductList(List<AeRtaProductInfoVO> productList) {
        this.productList = productList;
    }
}
