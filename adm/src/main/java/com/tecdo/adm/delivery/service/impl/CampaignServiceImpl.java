package com.tecdo.adm.delivery.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.dto.SimpleCampaignDTO;
import com.tecdo.adm.api.delivery.entity.Adv;
import com.tecdo.adm.api.delivery.entity.Campaign;
import com.tecdo.adm.api.delivery.entity.CampaignRtaInfo;
import com.tecdo.adm.api.delivery.mapper.CampaignMapper;
import com.tecdo.adm.api.delivery.vo.BaseCampaignVO;
import com.tecdo.adm.api.delivery.vo.CampaignRtaVO;
import com.tecdo.adm.api.delivery.vo.CampaignVO;
import com.tecdo.adm.api.delivery.vo.SimpleCampaignUpdateVO;
import com.tecdo.adm.common.cache.AdvCache;
import com.tecdo.adm.delivery.service.IAdGroupService;
import com.tecdo.adm.delivery.service.ICampaignRtaService;
import com.tecdo.adm.delivery.service.ICampaignService;
import com.tecdo.adm.log.service.IBizLogApiService;
import com.tecdo.starter.mp.entity.StatusEntity;
import com.tecdo.starter.mp.enums.BaseStatusEnum;
import com.tecdo.starter.mp.vo.BaseVO;
import com.tecdo.starter.tool.util.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/3/6
 */
@Service
@RequiredArgsConstructor
public class CampaignServiceImpl extends ServiceImpl<CampaignMapper, Campaign> implements ICampaignService {

    private final ICampaignRtaService campaignRtaService;
    private final IAdGroupService adGroupService;
    private final IBizLogApiService bizLogApiService;

    @Override
    @Transactional
    public boolean add(CampaignVO vo) {
        save(vo);
        CampaignRtaVO campaignRtaVO = vo.getCampaignRtaVO();
        if (campaignRtaVO != null) {
            campaignRtaService.save(campaignRtaVO);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean edit(CampaignVO vo) {
        if (vo.getId() == null) {
            return false;
        }
        logByUpdate(vo);
        if (updateById(vo)) {
            CampaignRtaVO campaignRtaVO = vo.getCampaignRtaVO();
            if (campaignRtaVO == null) {
                campaignRtaService.deleteByCampaignIds(Collections.singletonList(vo.getId()));
                return true;
            }
            return campaignRtaService.updateById(campaignRtaVO);
        }
        return false;
    }

    private void logByUpdate(CampaignVO afterVO) {
        Campaign campaign = getById(afterVO.getId());
        CampaignVO beforeVO = Objects.requireNonNull(BeanUtil.copy(campaign, CampaignVO.class));
        if (beforeVO.getAdvId() != null) {
            Adv adv = AdvCache.getAdv(beforeVO.getAdvId());
            if (adv != null) {
                beforeVO.setAdvName(adv.getName());
                beforeVO.setAdvType(adv.getType());
            }
        }
        CampaignRtaInfo campaignRta = campaignRtaService.getByCampaignId(beforeVO.getId());
        if (campaignRta != null) {
            CampaignRtaVO campaignRtaVO = Objects.requireNonNull(BeanUtil.copy(campaignRta, CampaignRtaVO.class));
            beforeVO.setCampaignRtaVO(campaignRtaVO);
        }
        bizLogApiService.logByUpdateCampaign(beforeVO, afterVO);
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
    @Transactional
    public boolean logicDelete(List<Integer> ids) {
        if (CollUtil.isEmpty(ids)) return false;
        Date date = new Date();
        List<StatusEntity> campaignStatusList = listStatus(ids);
        List<Campaign> entities = ids.stream().map(id -> {
            Campaign entity = new Campaign();
            entity.setId(id);
            entity.setStatus(BaseStatusEnum.DELETE.getType());
            entity.setUpdateTime(date);
            return entity;
        }).collect(Collectors.toList());
        updateBatchById(entities);
        List<Integer> adGroupIds = adGroupService.listIdByCampaignIds(ids);
        adGroupService.logicDelete(adGroupIds);
        bizLogApiService.logByDeleteCampaign(ids, campaignStatusList);
        return true;
    }

    private List<StatusEntity> listStatus(List<Integer> ids) {
        return baseMapper.listStatus(ids);
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
        Campaign entity = getById(vo.getId());
        if (entity == null) {
            return false;
        }
        CampaignVO beforeVO = Objects.requireNonNull(BeanUtil.copy(entity, CampaignVO.class));
        entity.setName(vo.getName());
        entity.setDailyBudget(vo.getDailyBudget());
        entity.setRemark(vo.getRemark());
        entity.setStatus(vo.getStatus());
        entity.setUpdateTime(new Date());
        CampaignVO afterVO = Objects.requireNonNull(BeanUtil.copy(entity, CampaignVO.class));
        bizLogApiService.logByUpdateCampaignDirect(beforeVO, afterVO);
        return updateById(entity);
    }

    @Override
    public IPage<Campaign> customPage(IPage<Campaign> page, Campaign campaign,
                                      List<Integer> advIds,
                                      List<Integer> adGroupIds, String adGroupName,
                                      List<Integer> adIds, String adName) {
        return baseMapper.customPage(page, campaign, advIds, adGroupIds, adGroupName, adIds, adName);
    }

    @Override
    public List<Integer> listIdByAdvIds(List<Integer> advIds) {
        return baseMapper.listIdByAdvIds(advIds);
    }
}
