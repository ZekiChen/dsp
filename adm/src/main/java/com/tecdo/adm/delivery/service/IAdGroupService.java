package com.tecdo.adm.delivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.vo.AdGroupVO;

import java.util.List;

/**
 * Created by Zeki on 2023/3/6
 */
public interface IAdGroupService extends IService<AdGroup> {

    boolean add(AdGroupVO vo);

    boolean edit(AdGroupVO vo);

    boolean delete(List<Integer> ids);
    void deleteByCampaignIds(List<Integer> campaignIds);

}
