package com.tecdo.adm.log.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.vo.*;
import com.tecdo.adm.api.log.entity.BizLogApi;
import com.tecdo.starter.mp.entity.StatusEntity;

import java.util.List;
import java.util.Map;

/**
 * Created by Zeki on 2023/4/5
 */
public interface IBizLogApiService extends IService<BizLogApi> {

    void logByUpdateAdGroup(AdGroupVO beforeVO, AdGroupVO afterVO);

    void logByDeleteAdGroup(List<Integer> ids, List<StatusEntity> adGroupStatusList);

    void logByUpdateAdGroupDirect(AdGroupVO beforeVO, AdGroupVO afterVO);

    void logByUpdateAdGroupBundle(List<TargetCondition> befores, List<TargetCondition> afters);

    void logByUpdateBatchAdGroup(Map<Integer, AdGroup> beAdGroupMap, BatchAdGroupUpdateVO afterVO);

    void logByUpdateBatchCondition(String attribute, Map<Integer, TargetCondition> beConditonMap,
                                   BundleAdGroupUpdateVO afterVO);

    // ===============================================================================
    void logByUpdateCampaign(CampaignVO beforeVO, CampaignVO afterVO);

    void logByDeleteCampaign(List<Integer> ids, List<StatusEntity> campaignStatusList);

    void logByUpdateCampaignDirect(CampaignVO beforeVO, CampaignVO afterVO);

    // ===============================================================================
    void logByUpdateAd(AdVO beforeVO, AdVO afterVO);

    void logByDeleteAd(List<Integer> ids, List<StatusEntity> adStatusList);

    void logByUpdateBatchAd(Map<Integer, Ad> beAdMap, BatchAdUpdateVO afterVO);
}
