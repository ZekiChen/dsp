package com.tecdo.adm.delivery.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.MultiBidStrategy;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.BidStrategyEnum;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.adm.api.delivery.mapper.AdGroupMapper;
import com.tecdo.adm.api.delivery.vo.*;
import com.tecdo.adm.common.cache.AdGroupCache;
import com.tecdo.adm.delivery.service.IAdGroupService;
import com.tecdo.adm.delivery.service.IAdService;
import com.tecdo.adm.delivery.service.IMultiBidStrategyService;
import com.tecdo.adm.delivery.service.ITargetConditionService;
import com.tecdo.adm.doris.IRequestService;
import com.tecdo.adm.log.service.IBizLogApiService;
import com.tecdo.common.util.UrlParamUtil;
import com.tecdo.starter.log.exception.ServiceException;
import com.tecdo.starter.mp.entity.BaseEntity;
import com.tecdo.starter.mp.entity.IdEntity;
import com.tecdo.starter.mp.entity.StatusEntity;
import com.tecdo.starter.mp.enums.BaseStatusEnum;
import com.tecdo.starter.mp.vo.BaseVO;
import com.tecdo.starter.tool.BigTool;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.tecdo.adm.api.delivery.enums.ConditionEnum.*;

/**
 * Created by Zeki on 2023/3/6
 */
@Service
@RequiredArgsConstructor
public class AdGroupServiceImpl extends ServiceImpl<AdGroupMapper, AdGroup> implements IAdGroupService {

    private final ITargetConditionService conditionService;
    private final IAdService adService;
    private final IBizLogApiService bizLogApiService;
    private final IRequestService requestService;
    private final IMultiBidStrategyService strategyService;

    @Value("${pac.condition.device-count.period}")
    private Integer deviceCntPeriod;

    @Override
    @Transactional
    public boolean add(AdGroupVO vo) {
        if (StrUtil.isNotBlank(vo.getClickUrl())) {
            vo.setForceLink(vo.getClickUrl());  // 测试时发现 deeplink 无法实现强跳
            String offerId = UrlParamUtil.getValue(vo.getClickUrl(), "offer_id");
            if (StrUtil.isNotBlank(offerId)) {
                vo.setOfferId(Integer.parseInt(offerId));
            }
        }
        return save(vo) && strategyService.saveBatch(vo.listStrategies()) && conditionService.saveBatch(vo.listCondition());
    }

    @Override
    @Transactional
    public boolean edit(AdGroupVO vo) {
        if (vo.getId() != null) {
            if (StrUtil.isNotBlank(vo.getClickUrl())) {
                vo.setForceLink(vo.getClickUrl());
                String offerId = UrlParamUtil.getValue(vo.getClickUrl(), "offer_id");
                if (StrUtil.isNotBlank(offerId)) {
                    vo.setOfferId(Integer.parseInt(offerId));
                }
            }
            logByUpdate(vo);
            if (updateById(vo)) {
                Map<Integer, List<String>> groupIdToAttrMap = new HashMap<>();
                List<AdGroupVO> adGroupVOList = Collections.singletonList(vo);
                // adGroupId -> attributes（多个用逗号分割）
                groupIdToAttrMap = adGroupVOList.stream()
                        .collect(Collectors.toMap(AdGroupVO::getId, adGroupVO -> {
                            List<TargetConditionVO> conditionVOs = adGroupVO.getConditionVOs();
                            return conditionVOs.stream()
                                    .map(TargetConditionVO::getAttribute)
                                    .collect(Collectors.toList());
                        }));

                conditionService.deleteByAdGroupIdsAndAttr(groupIdToAttrMap);
                conditionService.insertConditionsIfHasVal(vo.listCondition());
                strategyService.insertOrUpdate(vo.listStrategies());
                return true;
            }
        }
        return false;
    }

    private void logByUpdate(AdGroupVO afterVO) {
        Integer adGroupId = afterVO.getId();

        AdGroup adGroup = getById(adGroupId);
        AdGroupVO beforeVO = Objects.requireNonNull(com.tecdo.starter.tool.util.BeanUtil.copy(adGroup, AdGroupVO.class));

        List<TargetCondition> conditions = conditionService.listCondition(adGroupId);
        List<TargetConditionVO> conditionVOs = Objects.requireNonNull(com.tecdo.starter.tool.util.BeanUtil.copy(conditions, TargetConditionVO.class));
        beforeVO.setConditionVOs(conditionVOs);

        List<MultiBidStrategy> strategies = strategyService.listByAdGroupId(Collections.singletonList(adGroupId));
        List<MultiBidStrategyVO> strategyVOs = null;
        if (CollUtil.isNotEmpty(strategies)) {
            strategyVOs = Objects.requireNonNull(com.tecdo.starter.tool.util.BeanUtil.copy(strategies, MultiBidStrategyVO.class));;
        }
        beforeVO.setStrategyVOs(strategyVOs);

        bizLogApiService.logByUpdateAdGroup(beforeVO, afterVO);
    }

    @Override
    @Transactional
    public boolean delete(List<Integer> ids) {
        removeBatchByIds(ids);
        conditionService.deleteByAdGroupIds(ids);
        strategyService.deleteByAdGroupIds(ids);
        adService.deleteByAdGroupIds(ids);
        return true;
    }

    @Override
    @Transactional
    public boolean logicDelete(List<Integer> ids) {
        if (CollUtil.isEmpty(ids)) return false;
        Date date = new Date();
        List<StatusEntity> adGroupStatusList = listStatus(ids);
        List<AdGroup> entities = ids.stream().map(id -> {
            AdGroup entity = new AdGroup();
            entity.setId(id);
            entity.setStatus(BaseStatusEnum.DELETE.getType());
            entity.setUpdateTime(date);
            return entity;
        }).collect(Collectors.toList());
        updateBatchById(entities);
        List<Integer> adIds = adService.listIdByGroupIds(ids);
        adService.logicDelete(adIds);
        bizLogApiService.logByDeleteAdGroup(ids, adGroupStatusList);
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
    public boolean copy(Integer targetCampaignId, String sourceAdGroupIds, Integer copyNum, Integer targetAdGroupStatus) {
        if (copyNum < 1) {
            throw new ServiceException("copy num must bigger than 0!");
        }
        List<String> sourceAdGroupIdList = BigTool.toStrList(sourceAdGroupIds);
        List<AdGroup> sourceAdGroups = listByIds(sourceAdGroupIdList);
        if (CollUtil.isEmpty(sourceAdGroups)) {
            throw new ServiceException("source ad group is not exist!");
        }
        for (AdGroup sourceAdGroup : sourceAdGroups) {
            List<TargetCondition> sourceConditions = AdGroupCache.listCondition(sourceAdGroup.getId());
            if (CollUtil.isEmpty(sourceConditions)) {
                throw new ServiceException("source conditions is empty!");
            }
            sourceConditions = sourceConditions.stream()
                    .filter(cond -> !AUTO_BUNDLE.getDesc().equals(cond.getAttribute()) && !AUTO_BUNDLE_EXCEPT.getDesc().equals(cond.getAttribute()))
                    .collect(Collectors.toList());

            List<Integer> sourceAdIds = adService.listIdByGroupIds(Collections.singletonList(sourceAdGroup.getId()));
            List<MultiBidStrategy> sourceStrategies = strategyService.listByAdGroupId(Collections.singletonList(sourceAdGroup.getId()));

            replaceAdGroup(sourceAdGroup, targetCampaignId, targetAdGroupStatus);
            List<AdGroup> targetAdGroups = copyAdGroups(sourceAdGroup, copyNum);
            saveBatch(targetAdGroups);

            List<TargetCondition> targetConditions = replaceAndCopyConditions(targetAdGroups, sourceConditions);
            conditionService.saveBatch(targetConditions);

            if (CollUtil.isNotEmpty(sourceAdIds)) {
                List<Ad> sourceAds = adService.listByIds(sourceAdIds);
                List<Ad> targetAds = replaceAndCopyAds(targetAdGroups, sourceAds);
                adService.saveBatch(targetAds);
            }

            if (CollUtil.isNotEmpty(sourceStrategies)) {
                List<MultiBidStrategy> targetStrategies = replaceAndCopyStrategies(targetAdGroups, sourceStrategies);
                strategyService.saveBatch(targetStrategies);
            }
        }
        return true;
    }

    @Override
    public boolean editListInfo(SimpleAdGroupUpdateVO vo) {
        AdGroup entity = getById(vo.getId());
        if (entity == null) {
            return false;
        }
        AdGroupVO beforeVO = Objects.requireNonNull(BeanUtil.copyProperties(entity, AdGroupVO.class));
        entity.setName(vo.getName());
        entity.setDailyBudget(vo.getDailyBudget());
        entity.setBidStrategy(vo.getBidStrategy());
        entity.setOptPrice(vo.getOptPrice());
        entity.setBidMultiplier(vo.getBidMultiplier());
        entity.setRemark(vo.getRemark());
        entity.setStatus(vo.getStatus());
        entity.setUpdateTime(new Date());
        AdGroupVO afterVO = Objects.requireNonNull(BeanUtil.copyProperties(entity, AdGroupVO.class));
        bizLogApiService.logByUpdateAdGroupDirect(beforeVO, afterVO);
        return updateById(entity);
    }

    @Override
    public IPage<AdGroup> customPage(IPage<AdGroup> page, AdGroup adGroup,
                                     List<Integer> campaignIds, String campaignName,
                                     List<Integer> adIds, String adName,
                                     List<String> affiliateIds,
                                     List<String> countries) {
        Integer targetNum = 0;
        if (affiliateIds != null && !affiliateIds.isEmpty()) {
            targetNum++;
        }
        if (countries != null && !countries.isEmpty()) {
            targetNum++;
        }
        return baseMapper.customPage(page, adGroup, campaignIds, campaignName, adIds, adName, affiliateIds, countries, targetNum);
    }

    @Override
    @Transactional
    public boolean updateBundles(List<TargetCondition> conditions) {
        LambdaQueryWrapper<TargetCondition> wrapper = Wrappers.<TargetCondition>lambdaQuery()
                .eq(TargetCondition::getAdGroupId, conditions.get(0).getAdGroupId())
                .in(TargetCondition::getAttribute, ConditionEnum.BUNDLE.getDesc(),
                        ConditionEnum.AUTO_BUNDLE_EXCEPT.getDesc());
        List<TargetCondition> befores = conditionService.list(wrapper);
        if (CollUtil.isNotEmpty(befores)) {
            conditionService.remove(wrapper);
        }
        conditions.forEach(condition -> {
            if (StrUtil.isNotBlank(condition.getValue())) {
                conditionService.save(condition);
            }
        });
        batchUpdateTime(Collections.singletonList(conditions.get(0).getAdGroupId()));
        bizLogApiService.logByUpdateAdGroupBundle(befores, conditions);
        return true;
    }

    @Override
    public List<TargetCondition> listBundle(Integer adGroupId) {
        LambdaQueryWrapper<TargetCondition> wrapper = Wrappers.<TargetCondition>lambdaQuery()
                .eq(TargetCondition::getAdGroupId, adGroupId)
                .in(TargetCondition::getAttribute, ConditionEnum.BUNDLE.getDesc(),
                        ConditionEnum.AUTO_BUNDLE.getDesc(),
                        ConditionEnum.AUTO_BUNDLE_EXCEPT.getDesc());
        return conditionService.list(wrapper);
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
    @Transactional
    public boolean updateBatch(BatchAdGroupUpdateVO vo) {
        List<Integer> adGroupIds = vo.getAdGroupIds();
        List<AdGroup> adGroups = listByIds(adGroupIds);
        if (vo.getBidMultiplier() != null) {
            boolean match = adGroups.stream().anyMatch(e -> BidStrategyEnum.DYNAMIC.getType() != e.getBidStrategy());
            if (match) {
                throw new ServiceException("batch update bid multiplier does not allow non-BPC Strategy");
            }
        }
        Map<Integer, AdGroup> beAdGroupMap = adGroups.stream().collect(Collectors.toMap(IdEntity::getId, Function.identity()));
        List<AdGroup> updateAdGroups = vo.getAdGroupIds().stream().map(id -> {
            AdGroup entity = new AdGroup();
            entity.setId(id);
            entity.setStatus(vo.getStatus());
            entity.setOptPrice(vo.getOptPrice());
            entity.setBidStrategy(vo.getBidStrategy());
            entity.setDailyBudget(vo.getDailyBudget());
            entity.setBidMultiplier(vo.getBidMultiplier());
            entity.setForceJumpEnable(vo.getForceJumpEnable());
            entity.setForceJumpRatio(vo.getForceJumpRatio());
            entity.setUpdateTime(new Date());
            return entity;
        }).collect(Collectors.toList());
        updateBatchById(updateAdGroups);
        bizLogApiService.logByUpdateBatchAdGroup(beAdGroupMap, vo);
        return true;
    }

    @Override
    public List<Integer> listIdByAdvIds(List<Integer> advIds) {
        return baseMapper.listIdByAdvIds(advIds);
    }

    @Override
    public List<Integer> listIdByCampaignIds(List<Integer> campaignIds) {
        if (CollUtil.isEmpty(campaignIds)) {
            return new ArrayList<>();
        }
        return baseMapper.listIdByCampaignIds(campaignIds);
    }

    @Override
    @Transactional
    public boolean bundleUpdateBatch(BundleAdGroupUpdateVO vo) {
        List<Integer> adGroupIds = vo.getAdGroupIds();
        LambdaQueryWrapper<TargetCondition> wrapper = Wrappers.<TargetCondition>lambdaQuery()
                .in(TargetCondition::getAdGroupId, adGroupIds)
                .in(TargetCondition::getAttribute, ConditionEnum.BUNDLE.getDesc());
        Map<Integer, TargetCondition> beConditonMap = conditionService.list(wrapper).stream()
                .collect(Collectors.toMap(TargetCondition::getAdGroupId, Function.identity()));
        if (!beConditonMap.isEmpty()) {
            conditionService.remove(wrapper);
        }
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
        batchUpdateTime(adGroupIds);
        bizLogApiService.logByUpdateBatchCondition("Bundle", beConditonMap, vo);
        return true;
    }

    @Override
    @Transactional
    public boolean hourUpdateBatch(BundleAdGroupUpdateVO vo) {
        List<Integer> adGroupIds = vo.getAdGroupIds();
        LambdaQueryWrapper<TargetCondition> wrapper = Wrappers.<TargetCondition>lambdaQuery()
                .in(TargetCondition::getAdGroupId, adGroupIds)
                .in(TargetCondition::getAttribute, ConditionEnum.HOUR.getDesc());
        Map<Integer, TargetCondition> beConditonMap = conditionService.list(wrapper).stream()
                .collect(Collectors.toMap(TargetCondition::getAdGroupId, Function.identity()));
        if (!beConditonMap.isEmpty()) {
            conditionService.remove(wrapper);
        }
        if (StrUtil.isNotBlank(vo.getValue())) {
            List<TargetCondition> conditions = adGroupIds.stream().map(id -> {
                TargetCondition condition = new TargetCondition();
                condition.setAdGroupId(id);
                condition.setAttribute(ConditionEnum.HOUR.getDesc());
                condition.setOperation(vo.getOperation());
                condition.setValue(vo.getValue());
                return condition;
            }).collect(Collectors.toList());
            conditionService.saveBatch(conditions);
        }
        batchUpdateTime(adGroupIds);
        bizLogApiService.logByUpdateBatchCondition("Hour", beConditonMap, vo);
        return true;
    }

    @Override
    @Transactional
    public boolean fqcUpdateBatch(FqcAdGroupUpdateVO vo) {
        List<Integer> adGroupIds = vo.getAdGroupIds();
        String operation = vo.getOperation();

        // 更新
        updateBatchConditions(vo.getIsImpUpdate(), adGroupIds, ConditionEnum.IMP_FREQUENCY.getDesc(),
                operation, vo.getImpValue());
        updateBatchConditions(vo.getIsClickUpdate(), adGroupIds, ConditionEnum.CLICK_FREQUENCY.getDesc(),
                operation, vo.getClickValue());
        updateBatchConditions(vo.getIsImpUpdateByHour(), adGroupIds, ConditionEnum.IMP_FREQUENCY_HOUR.getDesc(),
                operation, vo.getImpValueByHour());
        updateBatchConditions(vo.getIsClickUpdateByHour(), adGroupIds, ConditionEnum.CLICK_FREQUENCY_HOUR.getDesc(),
                operation, vo.getClickValueByHour());

        batchUpdateTime(adGroupIds);
        return true;
    }

    @Override
    @Transactional
    public boolean limitBundleUpdateBatch(LimitBundleUpdateVO vo) {
        List<Integer> adGroupIds = vo.getAdGroupIds();
        String operation = vo.getOperation();

        // 更新
        updateBatchConditions(vo.getIsImpUpdate(), adGroupIds, BUNDLE_IMP_CAP_DAY.getDesc(),
                operation, vo.getImpValue());
        updateBatchConditions(vo.getIsClickUpdate(), adGroupIds, BUNDLE_CLICK_CAP_DAY.getDesc(),
                operation, vo.getClickValue());
        updateBatchConditions(vo.getIsCostUpdate(), adGroupIds, BUNDLE_COST_CAP_DAY.getDesc(),
                operation, vo.getCostValue());

        batchUpdateTime(adGroupIds);

        return true;
    }

    @Override
    @Transactional
    public boolean autoBundleUpdateBatch(AutoBundleUpdateVO vo) {
        List<Integer> adGroupIds = vo.getAdGroupIds();
        List<TargetConditionVO> conditionVOs = vo.getConditionVOs();
        Set<String> attrSet = new HashSet<>(Arrays.asList(BUNDLE_BLACK_IMP.getDesc(), BUNDLE_BLACK_CLICK.getDesc(),
                BUNDLE_BLACK_CTR.getDesc(), BUNDLE_BLACK_ROI.getDesc()));
        Set<String> attrUpdated = conditionVOs.stream()
                .map(TargetConditionVO::getAttribute)
                .collect(Collectors.toSet());

        // 删除未传入的condition条件
        attrSet.removeAll(attrUpdated);
        for (String attr : attrSet) {
            updateBatchConditions(true, adGroupIds, attr, "", ""); // value为空自动删除，并记录日志
        }

        // 更新传入的condition条件
        for (TargetConditionVO conditionVO : conditionVOs) {
            updateBatchConditions(true, adGroupIds, conditionVO.getAttribute(),
                    conditionVO.getOperation(), conditionVO.getValue());
        }

        batchUpdateTime(adGroupIds);

        return true;
    }

    private void batchUpdateTime(List<Integer> adGroupIds) {
        if (CollUtil.isNotEmpty(adGroupIds)) {
            List<AdGroup> adGroups = adGroupIds.stream().map(id -> {
                AdGroup adGroup = new AdGroup();
                adGroup.setId(id);
                return adGroup;
            }).collect(Collectors.toList());
            updateBatchById(adGroups);
        }
    }

    /**
     * 批量更新Condition，value为空则删除
     * @param isUpdated 为false则数据无变化
     * @param adGroupIds 待更新的adGroup列表
     * @param attribute condition-attribute
     * @param operation condition-operation
     * @param value condition-value
     */
    public void updateBatchConditions(Boolean isUpdated, List<Integer> adGroupIds, String attribute, String operation, String value) {
        if (!isUpdated) return;

        LambdaQueryWrapper<TargetCondition> wrapper = Wrappers.<TargetCondition>lambdaQuery()
                .in(TargetCondition::getAdGroupId, adGroupIds)
                .in(TargetCondition::getAttribute, attribute);

        // 获取更新前的 conditions
        Map<Integer, TargetCondition> beConditonMap = conditionService.list(wrapper).stream()
                .collect(Collectors.toMap(TargetCondition::getAdGroupId, Function.identity()));

        // 删除 conditions
        if (!beConditonMap.isEmpty()) {
            conditionService.remove(wrapper);
        }

        // value 不为空则更新，为空则默认删除
        if (StrUtil.isNotBlank(value)) {
            List<TargetCondition> conditions = adGroupIds.stream().map(id -> {
                TargetCondition condition = new TargetCondition();
                condition.setAdGroupId(id);
                condition.setAttribute(attribute);
                condition.setOperation(operation);
                condition.setValue(value);
                return condition;
            }).collect(Collectors.toList());
            conditionService.saveBatch(conditions);
        }

        BundleAdGroupUpdateVO bundleUpdateVO = new BundleAdGroupUpdateVO();
        bundleUpdateVO.setAdGroupIds(adGroupIds);
        bundleUpdateVO.setOperation(operation);
        bundleUpdateVO.setValue(value);
        bizLogApiService.logByUpdateBatchCondition(attribute, beConditonMap, bundleUpdateVO);
    }

    public List<StatusEntity> listStatus(List<Integer> ids) {
        return baseMapper.listStatus(ids);
    }

    @Override
    public String countDevice(List<TargetCondition> conditions) {
        if (CollUtil.isEmpty(conditions)) {
            return "0";
        }
        String startDate = DateUtil.offsetDay(new Date(), -deviceCntPeriod).toDateStr();
        String endDate = DateUtil.yesterday().toDateStr();
        List<String> affiliates = null;
        List<String> countries = null;
        List<String> deviceOSs = null;
        List<String> categories = null;
        List<String> tags = null;
        List<String> inBundles = new ArrayList<>();
        List<String> exBundles = new ArrayList<>();
        List<String> inDeviceMakes = new ArrayList<>();
        List<String> exDeviceMakes = new ArrayList<>();
        for (TargetCondition condition : conditions) {
            if (StrUtil.isBlank(condition.getValue()) || "null".equals(condition.getValue())) {
                continue;
            }
            switch (ConditionEnum.of(condition.getAttribute())) {
                case AFFILIATE:
                    affiliates = new ArrayList<>(BigTool.toStrList(condition.getValue()));
                    break;
                case BUNDLE:
                    List<String> bundles = BigTool.toStrList(condition.getValue());
                    if ("include".equals(condition.getOperation())) {
                        inBundles.addAll(bundles);
                    } else {
                        exBundles.addAll(bundles);
                    }
                    break;
                case CATEGORY:
                    categories = new ArrayList<>(BigTool.toStrList(condition.getValue()));
                    break;
                case TAG:
                    tags = new ArrayList<>(BigTool.toStrList(condition.getValue()));
                    break;
                case DEVICE_COUNTRY:
                    countries = new ArrayList<>(BigTool.toStrList(condition.getValue()));
                    break;
                case DEVICE_MAKE:
                    List<String> deviceMakes = BigTool.toStrList(condition.getValue());
                    if ("include".equals(condition.getOperation())) {
                        inDeviceMakes.addAll(deviceMakes);
                    } else {
                        exDeviceMakes.addAll(deviceMakes);
                    }
                    break;
                case DEVICE_OS:
                    deviceOSs = new ArrayList<>(BigTool.toStrList(condition.getValue()));
                    break;
                default:
                    break;
            }
        }
        try {
            if (CollUtil.isEmpty(categories) && CollUtil.isEmpty(tags)) {
                return requestService.countDevice(startDate, endDate,
                        affiliates, countries, inDeviceMakes, exDeviceMakes, deviceOSs, inBundles, exBundles);
            } else {
                return requestService.countDeviceWithGP(startDate, endDate,
                        affiliates, countries, inDeviceMakes, exDeviceMakes, deviceOSs, categories, tags, inBundles, exBundles);
            }
        } catch (Exception e) {
            return "unknown error";
        }
    }

    private static List<Ad> replaceAndCopyAds(List<AdGroup> targetAdGroups, List<Ad> sourceAds) {
        return targetAdGroups.stream()
                .flatMap(group -> sourceAds.stream().map(ad -> {
                    Ad newAd = BeanUtil.copyProperties(ad, Ad.class);
                    newAd.setGroupId(group.getId());
                    newAd.setStatus(ad.getStatus());
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

    private static List<MultiBidStrategy> replaceAndCopyStrategies(List<AdGroup> targetAdGroups,
                                                                   List<MultiBidStrategy> sourceStrategies) {
        return targetAdGroups.stream()
                .flatMap(group -> sourceStrategies.stream()
                        .map(strategy -> {
                            MultiBidStrategy newStrategy = BeanUtil.copyProperties(strategy, MultiBidStrategy.class);
                            newStrategy.setAdGroupId(group.getId());
                            resetBaseEntity(newStrategy);
                            return newStrategy;
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
