package com.tecdo.adm.api.delivery.dto;

import com.tecdo.adm.api.delivery.entity.Campaign;
import com.tecdo.adm.api.delivery.entity.CampaignRtaInfo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 与 AdDTO 相反，该类为聚合方向
 *
 * Created by Zeki on 2023/4/4
 */
@Setter
@Getter
public class CampaignDTO extends Campaign implements Serializable {

    private String advName;

    private List<AdGroupDTO> adGroupDTOs;

    private CampaignRtaInfo campaignRtaInfo;
}
