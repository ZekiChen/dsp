package com.tecdo.adm.delivery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.mapper.AdGroupMapper;
import com.tecdo.adm.api.delivery.vo.AdGroupVO;
import com.tecdo.adm.common.cache.AdCache;
import com.tecdo.adm.common.cache.AdGroupCache;
import com.tecdo.adm.delivery.service.IAdGroupService;
import com.tecdo.adm.delivery.service.IAdService;
import com.tecdo.adm.delivery.service.ITargetConditionService;
import com.tecdo.starter.mp.entity.BaseEntity;
import com.tecdo.starter.mp.vo.BaseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @Override
    public List<AdGroup> listByCampaignIds(List<Integer> campaignIds) {
        return baseMapper.selectList(Wrappers.<AdGroup>lambdaQuery().in(AdGroup::getCampaignId, campaignIds));
    }

    @Override
    @Transactional
    public boolean copy(Integer targetCampaignId, Integer sourceAdGroupId, Integer copyNum,
                        Integer targetAdGroupStatus, Integer targetAdStatus) {
        AdGroup sourceAdGroup = AdGroupCache.getAdGroup(sourceAdGroupId);
        List<TargetCondition> sourceConditions = AdGroupCache.listCondition(sourceAdGroupId);
        if (copyNum < 1 || sourceAdGroup == null || CollUtil.isEmpty(sourceConditions)) {
            return false;
        }
        List<Ad> sourceAds = AdCache.listAd(sourceAdGroupId);
        replaceAdGroup(sourceAdGroup, targetCampaignId, targetAdGroupStatus);
        List<AdGroup> targetAdGroups = copyAdGroups(sourceAdGroup, copyNum);
        saveBatch(targetAdGroups);
        List<TargetCondition> targetConditions = replaceAndCopyConditions(targetAdGroups, sourceConditions);
        conditionService.saveBatch(targetConditions);
        if (CollUtil.isNotEmpty(sourceAds)) {
            List<Ad> targetAds = replaceAndCopyAds(targetAdGroups, sourceAds, targetAdStatus);
            adService.saveBatch(targetAds);
        }
        return true;
    }

    @Override
    public boolean editListInfo(Integer id, Double optPrice, Double dailyBudget) {
        AdGroup adGroup = getById(id);
        if (adGroup == null) {
            return false;
        }
        adGroup.setOptPrice(optPrice);
        adGroup.setDailyBudget(dailyBudget);
        return updateById(adGroup);
    }

    @Override
    public IPage<AdGroup> customPage(IPage<AdGroup> page, AdGroup adGroup, List<Integer> campaignIds, String affiliateName) {
        return baseMapper.customPage(page, adGroup, campaignIds, affiliateName);
    }

    private static List<Ad> replaceAndCopyAds(List<AdGroup> targetAdGroups, List<Ad> sourceAds, Integer targetAdStatus) {
        return targetAdGroups.stream()
                .flatMap(group -> sourceAds.stream().map(ad -> {
                    Ad newAd = BeanUtil.copyProperties(ad, Ad.class);
                    newAd.setGroupId(group.getId());
                    newAd.setStatus(targetAdStatus);
                    resetBaseEntity(newAd);
                    return newAd;
                }))
                .collect(Collectors.toList());
    }

    private static void resetBaseEntity(BaseEntity entity) {
        entity.setId(null);
        entity.setCreateTime(null);
        entity.setUpdateTime(null);
    }

    private static List<TargetCondition> replaceAndCopyConditions(List<AdGroup> targetAdGroups,
                                                                  List<TargetCondition> sourceConditions) {
        return targetAdGroups.stream()
                .flatMap(group -> sourceConditions.stream()
                        .map(cond -> {
                            TargetCondition newCond = BeanUtil.copyProperties(cond, TargetCondition.class);
                            newCond.setAdGroupId(group.getId());
                            resetBaseEntity(newCond);
                            return newCond;
                        }))
                .collect(Collectors.toList());
    }

    private static void replaceAdGroup(AdGroup sourceAdGroup,
                                       Integer targetCampaignId, Integer targetAdGroupStatus) {
        resetBaseEntity(sourceAdGroup);
        sourceAdGroup.setCampaignId(targetCampaignId);
        sourceAdGroup.setStatus(targetAdGroupStatus);
    }

    private static List<AdGroup> copyAdGroups(AdGroup sourceAdGroup, Integer copyNum) {
        return IntStream.range(0, copyNum)
                .mapToObj(i -> BeanUtil.copyProperties(sourceAdGroup, AdGroup.class))
                .collect(Collectors.toList());
    }
}
