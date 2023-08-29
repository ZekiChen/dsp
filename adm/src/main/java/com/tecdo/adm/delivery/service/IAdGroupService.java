package com.tecdo.adm.delivery.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.vo.*;
import com.tecdo.starter.mp.entity.StatusEntity;
import com.tecdo.starter.mp.vo.BaseVO;

import java.util.List;

/**
 * Created by Zeki on 2023/3/6
 */
public interface IAdGroupService extends IService<AdGroup> {

    boolean add(AdGroupVO vo);

    boolean edit(AdGroupVO vo);

    boolean delete(List<Integer> ids);
    boolean logicDelete(List<Integer> ids);

    void deleteByCampaignIds(List<Integer> campaignIds);

    List<BaseVO> listIdAndName();

    List<AdGroup> listByCampaignIds(List<Integer> campaignIds);

    boolean copy(Integer targetCampaignId, String sourceAdGroupIds, Integer copyNum, Integer targetAdGroupStatus);

    boolean editListInfo(SimpleAdGroupUpdateVO vo);

    IPage<AdGroup> customPage(IPage<AdGroup> page, AdGroup adGroup,
                              List<Integer> campaignIds, String campaignName,
                              List<Integer> adIds, String adName,
                              List<String> affiliateIds,
                              List<String> countries);

    boolean updateBundles(TargetCondition condition);

    TargetCondition listBundle(Integer adGroupId);

    List<Integer> listIdByLikeCampaignName(String campaignName);

    List<Integer> listIdByLikeAdGroupName(String name);

    List<Integer> listAdGroupIdForListAd(String cIds, String gIds, String cName, String gName);

    boolean updateBatch(BatchAdGroupUpdateVO vo);

    List<Integer> listIdByAdvIds(List<Integer> advIds);
    List<Integer> listIdByCampaignIds(List<Integer> campaignIds);

    boolean bundleUpdateBatch(BundleAdGroupUpdateVO vo);
    boolean hourUpdateBatch(BundleAdGroupUpdateVO vo);

    boolean fqcUpdateBatch(FqcAdGroupUpdateVO vo);
    List<StatusEntity> listStatus(List<Integer> ids);

    String countDevice(List<TargetCondition> conditions);
}