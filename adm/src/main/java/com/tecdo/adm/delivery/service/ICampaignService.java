package com.tecdo.adm.delivery.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tecdo.adm.api.delivery.entity.Campaign;
import com.tecdo.adm.api.delivery.vo.BaseCampaignVO;
import com.tecdo.adm.api.delivery.vo.CampaignVO;
import com.tecdo.adm.api.delivery.vo.SimpleCampaignUpdateVO;
import com.tecdo.starter.mp.vo.BaseVO;

import java.util.List;

/**
 * Created by Zeki on 2023/3/6
 */
public interface ICampaignService extends IService<Campaign> {

    boolean add(CampaignVO vo);

    boolean edit(CampaignVO vo);

    boolean delete(List<Integer> ids);

    List<BaseVO> listIdAndName();

    List<BaseCampaignVO> listCampaignWithGroupIdName();

    boolean editListInfo(SimpleCampaignUpdateVO vo);

    IPage<Campaign> customPage(IPage<Campaign> page, Campaign campaign,
                               List<Integer> advIds,
                               List<Integer> adGroupIds, String adGroupName,
                               List<Integer> adIds, String adName);
}
