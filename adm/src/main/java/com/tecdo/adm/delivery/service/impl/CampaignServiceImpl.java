package com.tecdo.adm.delivery.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.Campaign;
import com.tecdo.adm.api.delivery.mapper.CampaignMapper;
import com.tecdo.adm.api.delivery.vo.CampaignRtaVO;
import com.tecdo.adm.api.delivery.vo.CampaignVO;
import com.tecdo.adm.delivery.service.ICampaignRtaService;
import com.tecdo.adm.delivery.service.ICampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Created by Zeki on 2023/3/6
 */
@Service
@RequiredArgsConstructor
public class CampaignServiceImpl extends ServiceImpl<CampaignMapper, Campaign> implements ICampaignService {

    private final ICampaignRtaService campaignRtaService;

    @Override
    public boolean add(CampaignVO vo) {
        if (save(vo)) {
            CampaignRtaVO campaignRtaVO = vo.getCampaignRtaVO();
            if (campaignRtaVO == null) {
                return true;
            }
            campaignRtaVO.setCampaignId(vo.getId());
            return campaignRtaService.save(campaignRtaVO);
        }
        return false;
    }

    @Override
    public boolean edit(CampaignVO vo) {
        if (vo.getId() != null && updateById(vo)) {
            CampaignRtaVO campaignRtaVO = vo.getCampaignRtaVO();
            if (campaignRtaVO == null) {
                campaignRtaService.deleteByCampaignIds(Collections.singletonList(vo.getId()));
                return true;
            }
            campaignRtaVO.setCampaignId(vo.getId());
            return campaignRtaService.updateById(campaignRtaVO);
        }
        return false;
    }

    @Override
    public boolean delete(List<Integer> ids) {
        return removeByIds(ids) && campaignRtaService.deleteByCampaignIds(ids);
    }
}
