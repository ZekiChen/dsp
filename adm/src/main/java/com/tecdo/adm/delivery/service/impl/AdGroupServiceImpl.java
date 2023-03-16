package com.tecdo.adm.delivery.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.mapper.AdGroupMapper;
import com.tecdo.adm.api.delivery.vo.AdGroupVO;
import com.tecdo.adm.delivery.service.IAdGroupService;
import com.tecdo.adm.delivery.service.IAdService;
import com.tecdo.adm.delivery.service.ITargetConditionService;
import com.tecdo.starter.mp.vo.BaseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/3/6
 */
@Service
@RequiredArgsConstructor
public class AdGroupServiceImpl extends ServiceImpl<AdGroupMapper, AdGroup> implements IAdGroupService {

    private final ITargetConditionService conditionService;
    private final IAdService adService;

    @Override
    public boolean add(AdGroupVO vo) {
        return save(vo) && conditionService.saveBatch(vo.listCondition());
    }

    @Override
    public boolean edit(AdGroupVO vo) {
        if (vo.getId() != null && updateById(vo)) {
            conditionService.deleteByAdGroupIds(Collections.singletonList(vo.getId()));
            conditionService.saveBatch(vo.listCondition());
            return true;
        }
        return false;
    }

    @Override
    public boolean delete(List<Integer> ids) {
        removeBatchByIds(ids);
        conditionService.deleteByAdGroupIds(ids);
        adService.deleteByAdGroupIds(ids);
        return true;
    }

    @Override
    public void deleteByCampaignIds(List<Integer> campaignIds) {
        List<AdGroup> adGroups = baseMapper.selectList(Wrappers.<AdGroup>lambdaQuery().in(AdGroup::getCampaignId, campaignIds));
        List<Integer> adGroupIds = adGroups.stream().map(AdGroup::getId).collect(Collectors.toList());
        delete(adGroupIds);
    }

    @Override
    public List<BaseVO> listIdAndName() {
        return baseMapper.listIdAndName();
    }
}
