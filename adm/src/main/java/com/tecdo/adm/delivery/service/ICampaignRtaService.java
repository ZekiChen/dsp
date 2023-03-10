package com.tecdo.adm.delivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.delivery.entity.CampaignRtaInfo;

import java.util.List;

/**
 * Created by Zeki on 2023/3/6
 */
public interface ICampaignRtaService extends IService<CampaignRtaInfo> {

    boolean deleteByCampaignIds(List<Integer> campaignIds);

    CampaignRtaInfo getByCampaignId(Integer campaignId);
}
