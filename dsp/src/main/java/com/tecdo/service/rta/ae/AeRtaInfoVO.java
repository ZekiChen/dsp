package com.tecdo.service.rta.ae;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Zeki on 2023/4/4
 */
@Setter
@Getter
public class AeRtaInfoVO implements Serializable {

    private String advCampaignId;
    private Boolean target;

    /**
     * @see AeMaterialTypeEnum
     */
    private String materialType;

    private String landingPage;
    private String deeplink;
}
