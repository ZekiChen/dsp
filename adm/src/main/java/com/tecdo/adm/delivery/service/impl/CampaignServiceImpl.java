package com.tecdo.adm.delivery.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.dto.SimpleCampaignDTO;
import com.tecdo.adm.api.delivery.entity.Campaign;
import com.tecdo.adm.api.delivery.mapper.CampaignMapper;
import com.tecdo.adm.api.delivery.vo.BaseCampaignVO;
import com.tecdo.adm.api.delivery.vo.CampaignRtaVO;
import com.tecdo.adm.api.delivery.vo.CampaignVO;
import com.tecdo.adm.api.delivery.vo.SimpleCampaignUpdateVO;
import com.tecdo.adm.delivery.service.IAdGroupService;
import com.tecdo.adm.delivery.service.ICampaignRtaService;
import com.tecdo.adm.delivery.service.ICampaignService;
import com.tecdo.starter.mp.vo.BaseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/3/6
 */
@Service
@RequiredArgsConstructor
public class CampaignServiceImpl extends ServiceImpl<CampaignMapper, Campaign> implements ICampaignService {

    private final ICampaignRtaService campaignRtaService;
    private final IAdGroupService adGroupService;

    @Override
    @Transactional
    public boolean add(CampaignVO vo) {
        save(vo);
        CampaignRtaVO campaignRtaVO = vo.getCampaignRtaVO();
        if (campaignRtaVO != null) {
            campaignRtaVO.setAdvMemId(campaignRtaVO.getAdvId());
            campaignRtaService.save(campaignRtaVO);
        }
        return true;
    }

    @Override
    public boolean edit(CampaignVO vo) {
        if (vo.getId() != null && updateById(vo)) {
            CampaignRtaVO campaignRtaVO = vo.getCampaignRtaVO();
            if (campaignRtaVO == null) {
                campaignRtaService.deleteByCampaignIds(Collections.singletonList(vo.getId()));
                return true;
            }
            campaignRtaVO.setAdvMemId(campaignRtaVO.getAdvId());
            return campaignRtaService.updateById(campaignRtaVO);
        }
        return false;
    }

    @Override
    public boolean delete(List<Integer> ids) {
        if (removeByIds(ids)) {
            campaignRtaService.deleteByCampaignIds(ids);
            adGroupService.deleteByCampaignIds(ids);
            return true;
        }
        return false;
    }

    @Override
    public List<BaseVO> listIdAndName() {
        return baseMapper.listIdAndName();
    }

    @Override
    public List<BaseCampaignVO> listCampaignWithGroupIdName() {
        List<SimpleCampaignDTO> simpleCampaignDTOs = baseMapper.listCampaignWithGroupIdName();
        return simpleCampaignDTOs.stream()
                .collect(Collectors.groupingBy(SimpleCampaignDTO::getCampaignId))
                .entrySet().stream()
                .map(entry -> {
                    BaseCampaignVO vo = new BaseCampaignVO();
                    vo.setId(entry.getKey());
                    vo.setName(entry.getValue().get(0).getCampaignName());
                    vo.setBaseAdGroupVOs(entry.getValue().stream()
                            .map(dto -> {
                                BaseVO baseVO = new BaseVO();
                                baseVO.setId(dto.getAdGroupId());
                                baseVO.setName(dto.getAdGroupName());
                                return baseVO;
                            })
                            .collect(Collectors.toList()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean editListInfo(SimpleCampaignUpdateVO vo) {
        Campaign campaign = getById(vo.getId());
        if (campaign == null) {
            return false;
        }
        campaign.setDailyBudget(vo.getDailyBudget());
        campaign.setStatus(vo.getStatus());
        campaign.setUpdateTime(new Date());
        return updateById(campaign);
    }

    @Override
    public IPage<Campaign> customPage(IPage<Campaign> page, Campaign campaign) {
        return baseMapper.customPage(page, campaign);
    }
}
