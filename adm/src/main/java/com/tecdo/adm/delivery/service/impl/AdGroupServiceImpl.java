package com.tecdo.adm.delivery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.adm.api.delivery.mapper.AdGroupMapper;
import com.tecdo.adm.api.delivery.vo.AdGroupVO;
import com.tecdo.adm.api.delivery.vo.BatchAdGroupUpdateVO;
import com.tecdo.adm.api.delivery.vo.BundleAdGroupUpdateVO;
import com.tecdo.adm.api.delivery.vo.SimpleAdGroupUpdateVO;
import com.tecdo.adm.common.cache.AdCache;
import com.tecdo.adm.common.cache.AdGroupCache;
import com.tecdo.adm.delivery.service.IAdGroupService;
import com.tecdo.adm.delivery.service.IAdService;
import com.tecdo.adm.delivery.service.ITargetConditionService;
import com.tecdo.starter.mp.entity.BaseEntity;
import com.tecdo.starter.mp.vo.BaseVO;
import com.tecdo.starter.tool.BigTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    public boolean editListInfo(SimpleAdGroupUpdateVO vo) {
        AdGroup entity = getById(vo.getId());
        if (entity == null) {
            return false;
        }
        entity.setName(vo.getName());
        entity.setDailyBudget(vo.getDailyBudget());
        entity.setBidStrategy(vo.getBidStrategy());
        entity.setOptPrice(vo.getOptPrice());
        entity.setStatus(vo.getStatus());
        entity.setUpdateTime(new Date());
        return updateById(entity);
    }

    @Override
    public IPage<AdGroup> customPage(IPage<AdGroup> page, AdGroup adGroup,
                                     List<Integer> campaignIds, String campaignName,
                                     List<Integer> adIds, String adName,
                                     List<String> affiliateIds,
                                     List<String> countries) {
        return baseMapper.customPage(page, adGroup, campaignIds, campaignName, adIds, adName, affiliateIds, countries);
    }

    @Override
    public boolean updateBundles(TargetCondition condition) {
        LambdaQueryWrapper<TargetCondition> wrapper = Wrappers.<TargetCondition>lambdaQuery()
                .eq(TargetCondition::getAdGroupId, condition.getAdGroupId())
                .eq(TargetCondition::getAttribute, ConditionEnum.BUNDLE.getDesc());
        conditionService.remove(wrapper);
        if (StrUtil.isNotBlank(condition.getValue())) {
            condition.setAttribute(ConditionEnum.BUNDLE.getDesc());
            conditionService.save(condition);
        }
        return true;
    }

    @Override
    public TargetCondition listBundle(Integer adGroupId) {
        LambdaQueryWrapper<TargetCondition> wrapper = Wrappers.<TargetCondition>lambdaQuery()
                .eq(TargetCondition::getAdGroupId, adGroupId)
                .eq(TargetCondition::getAttribute, ConditionEnum.BUNDLE.getDesc());
        return conditionService.getOne(wrapper);
    }

    @Override
    public List<Integer> listIdByLikeCampaignName(String campaignName) {
        return baseMapper.listIdByLikeCampaignName(campaignName);
    }

    @Override
    public List<Integer> listIdByLikeAdGroupName(String name) {
        return baseMapper.listIdByLikeAdGroupName(name);
    }

    @Override
    public List<Integer> listAdGroupIdForListAd(String cIds, String gIds, String cName, String gName) {
        List<Integer> adGroupIds = new ArrayList<>();
        if (StrUtil.isNotBlank(cIds)) {
            List<AdGroup> ids1 = listByCampaignIds(BigTool.toIntList(cIds));
            if (CollUtil.isEmpty(ids1)) {
                return null;
            }
            adGroupIds.addAll(ids1.stream().map(AdGroup::getId).collect(Collectors.toList()));
        }
        if (StrUtil.isNotBlank(gIds)) {
            List<Integer> ids2 = BigTool.toIntList(gIds);
            if (CollUtil.isEmpty(adGroupIds)) {
                adGroupIds.addAll(ids2);
            } else {
                Set<Integer> set = new HashSet<>(adGroupIds);
                adGroupIds = ids2.stream().filter(set::contains).collect(Collectors.toList());
                if (CollUtil.isEmpty(adGroupIds)) {
                    return null;
                }
            }
        }
        if (StrUtil.isNotBlank(cName)) {
            List<Integer> ids3 = listIdByLikeCampaignName(cName);
            if (CollUtil.isEmpty(ids3)) {
                return null;
            }
            if (CollUtil.isEmpty(adGroupIds)) {
                adGroupIds.addAll(ids3);
            } else {
                Set<Integer> set = new HashSet<>(adGroupIds);
                adGroupIds = ids3.stream().filter(set::contains).collect(Collectors.toList());
                if (CollUtil.isEmpty(adGroupIds)) {
                    return null;
                }
            }
        }
        if (StrUtil.isNotBlank(gName)) {
            List<Integer> ids4 = listIdByLikeAdGroupName(gName);
            if (CollUtil.isEmpty(ids4)) {
                return null;
            }
            if (CollUtil.isEmpty(adGroupIds)) {
                adGroupIds.addAll(ids4);
            } else {
                Set<Integer> set = new HashSet<>(adGroupIds);
                adGroupIds = ids4.stream().filter(set::contains).collect(Collectors.toList());
                if (CollUtil.isEmpty(adGroupIds)) {
                    return null;
                }
            }
        }
        return adGroupIds;
    }

    @Override
    public boolean updateBatch(BatchAdGroupUpdateVO vo) {
        List<AdGroup> adGroups = vo.getAdGroupIds().stream().map(id -> {
            AdGroup entity = new AdGroup();
            entity.setId(id);
            entity.setStatus(vo.getStatus());
            entity.setOptPrice(vo.getOptPrice());
            entity.setBidStrategy(vo.getBidStrategy());
            entity.setDailyBudget(vo.getDailyBudget());
            entity.setUpdateTime(new Date());
            return entity;
        }).collect(Collectors.toList());
        updateBatchById(adGroups);
        return true;
    }

    @Override
    public List<Integer> listIdByAdvIds(List<Integer> advIds) {
        return baseMapper.listIdByAdvIds(advIds);
    }

    @Override
    public boolean bundleUpdateBatch(BundleAdGroupUpdateVO vo) {
        List<Integer> adGroupIds = vo.getAdGroupIds();
        conditionService.deleteByAdGroupIds(adGroupIds);
        if (StrUtil.isNotBlank(vo.getValue())) {
            List<TargetCondition> conditions = adGroupIds.stream().map(id -> {
                TargetCondition condition = new TargetCondition();
                condition.setAdGroupId(id);
                condition.setAttribute(ConditionEnum.BUNDLE.getDesc());
                condition.setOperation(vo.getOperation());
                condition.setValue(vo.getValue());
                return condition;
            }).collect(Collectors.toList());
            conditionService.saveBatch(conditions);
        }
        return true;
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
                .mapToObj(i -> {
                    int count = i + 1;
                    AdGroup target = BeanUtil.copyProperties(sourceAdGroup, AdGroup.class);
                    target.setName(target.getName() + "-copy" + count);
                    return target;
                })
                .collect(Collectors.toList());
    }
}
