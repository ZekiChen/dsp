package com.tecdo.adm.delivery.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.CampaignRtaInfo;
import com.tecdo.adm.api.delivery.mapper.CampaignRtaInfoMapper;
import com.tecdo.adm.delivery.service.ICampaignRtaService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeki on 2023/3/6
 */
@Service
public class CampaignRtaServiceImpl extends ServiceImpl<CampaignRtaInfoMapper, CampaignRtaInfo> implements ICampaignRtaService {

    @Override
    public boolean deleteByCampaignIds(List<Integer> campaignIds) {
        baseMapper.delete(Wrappers.<CampaignRtaInfo>lambdaQuery().in(CampaignRtaInfo::getCampaignId, campaignIds));
        return true;
    }

    @Override
    public CampaignRtaInfo getByCampaignId(Integer campaignId) {
        return baseMapper.selectOne(Wrappers.<CampaignRtaInfo>lambdaQuery().eq(CampaignRtaInfo::getCampaignId, campaignId));
    }
}
