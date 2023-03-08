package com.tecdo.adm.delivery.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.Campaign;
import com.tecdo.adm.api.delivery.mapper.CampaignMapper;
import com.tecdo.adm.api.delivery.vo.CampaignVO;
import com.tecdo.adm.delivery.service.ICampaignService;
import org.springframework.stereotype.Service;

/**
 * Created by Zeki on 2023/3/6
 */
@Service
public class CampaignServiceImpl extends ServiceImpl<CampaignMapper, Campaign> implements ICampaignService {

    @Override
    public boolean add(CampaignVO campaignVO) {
        // TODO
        return false;
    }
}
