package com.tecdo.adm.doris;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.doris.entity.AdGroupCost;

import java.util.List;

/**
 * Created by Zeki on 2023/4/5
 */
public interface IImpCostService extends IService<AdGroupCost> {


    List<AdGroupCost> listByGroupIds(List<Integer> adGroupIds);
    List<AdGroupCost> listByCampaignIds(List<Integer> campaignIds);
}
