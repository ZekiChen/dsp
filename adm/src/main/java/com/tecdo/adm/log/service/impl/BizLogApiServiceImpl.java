package com.tecdo.adm.log.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.MultiBidStrategy;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.BidStrategyEnum;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.adm.api.delivery.vo.*;
import com.tecdo.adm.api.log.entity.BizLogApi;
import com.tecdo.adm.api.log.enums.BizTypeEnum;
import com.tecdo.adm.api.log.enums.OptTypeEnum;
import com.tecdo.adm.api.log.mapper.BizLogApiMapper;
import com.tecdo.adm.log.service.IBizLogApiService;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.starter.mp.entity.IdEntity;
import com.tecdo.starter.mp.entity.StatusEntity;
import com.tecdo.starter.mp.enums.BaseStatusEnum;
import com.tecdo.starter.tool.util.BeanUtil;
import com.tecdo.starter.tool.util.ClassUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/4/5
 */
@Service
@RequiredArgsConstructor
public class BizLogApiServiceImpl extends ServiceImpl<BizLogApiMapper, BizLogApi> implements IBizLogApiService {

    private final ThreadPool threadPool;

    public void logByUpdateAdGroup(AdGroupVO beforeVO, AdGroupVO afterVO) {
        threadPool.execute(() -> {
            StringBuilder sb = new StringBuilder();
            try {
                compareObj(sb, beforeVO, afterVO);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            List<TargetConditionVO> beConditionVOs = beforeVO.getConditionVOs();
            List<MultiBidStrategyVO> beStrategyVOs = beforeVO.getStrategyVOs();

            List<TargetCondition> afterConditions = afterVO.listCondition();
            if (CollUtil.isNotEmpty(afterConditions)) {
                Map<String, TargetCondition> afterCondMap = afterConditions.stream().collect(Collectors.toMap(TargetCondition::getAttribute, v -> v));
                for (TargetConditionVO before : beConditionVOs) {
                    if (afterCondMap.containsKey(before.getAttribute())) {
                        TargetCondition after = afterCondMap.get(before.getAttribute());
                        // before和after都有，但值不一致
                        if (!before.getOperation().equals(after.getOperation()) || !before.getValue().equals(after.getValue())) {
                            sb.append(before.getAttribute()).append(": ").append(before.getOperation()).append(" ")
                                    .append(before.getValue()).append(" -> ").append(after.getOperation()).append(" ").append(after.getValue()).append("\n");
                        }
                    } else {
                        // before有，after没有
                        sb.append(before.getAttribute()).append(": ").append(before.getOperation()).append(" ")
                                .append(before.getValue()).append(" -> null\n");
                    }
                }
                Map<String, TargetConditionVO> beforeMap = beConditionVOs.stream().collect(Collectors.toMap(TargetConditionVO::getAttribute, v -> v));
                for (TargetCondition after : afterConditions) {
                    // before没有，after有
                    if (!beforeMap.containsKey(after.getAttribute())) {
                        sb.append(after.getAttribute()).append(": null -> ")
                                .append(after.getOperation()).append(" ").append(after.getValue()).append("\n");
                    }
                }
            }

            List<MultiBidStrategy> afterStrategies = afterVO.listStrategies();
            if (CollUtil.isNotEmpty(afterStrategies)) {
                Field[] strategyFields = MultiBidStrategy.class.getDeclaredFields();
                Map<Integer, MultiBidStrategy> afterStrategyMap = afterStrategies.stream().collect(Collectors.toMap(MultiBidStrategy::getStage, v -> v));
                if (CollUtil.isNotEmpty(beStrategyVOs)) { //若before & after 都不为空
                    try {
                        for (MultiBidStrategyVO beStrategyVO : beStrategyVOs) {
                            int stage = beStrategyVO.getStage();
                            boolean isChanged = false;
                            StringBuilder tmp_sb = new StringBuilder();
                            tmp_sb.append("strategy for stage").append(stage).append(": ");
                            for (Field field : strategyFields) {
                                field.setAccessible(true);
                                Object beforeVal = field.get(beStrategyVO);
                                Object afterVal = field.get(afterStrategyMap.get(stage));
                                // 若存在一个修改则标记
                                if (!ObjectUtil.equal(beforeVal, afterVal)) {
                                    isChanged = true;
                                    tmp_sb.append("[").append(field.getName()).append("] ")
                                            .append(beforeVal).append(" -> ").append(afterVal).append("; ");
                                }
                            }
                            tmp_sb.append("\n");
                            if (isChanged) {
                                sb.append(tmp_sb);
                            }
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            if (sb.length() > 0) {
                BizLogApi bizLogApi = new BizLogApi();
                bizLogApi.setBizId(beforeVO.getId());
                bizLogApi.setOptType(OptTypeEnum.UPDATE.getType());
                bizLogApi.setBizType(BizTypeEnum.AD_GROUP.getType());
                bizLogApi.setTitle("Ad Group Update");
                bizLogApi.setContent(sb.substring(0, sb.length() - 1));
                bizLogApi.setCreator("admin");
                save(bizLogApi);
            }
        });
    }

    /**
     * 比较对象修改前后并写入修改日志
     * @param sb 日志字符串
     * @param beforeVO 修改前对象
     * @param afterVO 修改后对象
     */
    public void compareObj(StringBuilder sb, Object beforeVO, Object afterVO) throws IllegalAccessException {
        Class<?> clazz = afterVO.getClass();
        Field[] fields = clazz.getDeclaredFields();

        // 获取父类的私有字段
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            Field[] superFields = superClass.getDeclaredFields();
            fields = ArrayUtil.addAll(fields, superFields);
        }

        for (Field field : fields) {
            field.setAccessible(true);

            if (!ClassUtil.isPrimitiveOrWrapper(field.getType()) && !(field.getType() == String.class)) continue;
            String fieldName = field.getName();
            Object beforeValue = field.get(beforeVO);
            Object afterValue = field.get(afterVO);

            //为空则证明不被更新
            if (afterValue == null) continue;

            //对枚举字段做特殊处理
            switch (fieldName) {
                case "bidStrategy":
                    beforeValue = BidStrategyEnum.of((Integer) beforeValue).getDesc();
                    afterValue = BidStrategyEnum.of((Integer) afterValue).getDesc();
                    break;
                case "status":
                    beforeValue = BaseStatusEnum.of((Integer) beforeValue).getDesc();
                    afterValue = BaseStatusEnum.of((Integer) afterValue).getDesc();
                    break;
                default:
            }

            //写入记录
            if (!(ObjectUtil.equal(beforeValue, afterValue))) {
                sb.append("[").append(fieldName).append("] ")
                        .append(beforeValue).append(" -> ").append(afterValue).append(";").append("\n");
            }
        }
    }

    @Override
    public void logByDeleteAdGroup(List<Integer> ids, List<StatusEntity> adGroupStatusList) {
        Map<Integer, StatusEntity> beStatusMap = adGroupStatusList.stream().collect(Collectors.toMap(IdEntity::getId, Function.identity()));
        threadPool.execute(() -> {
            List<BizLogApi> bizLogApis = ids.stream().map(id -> {
                BizLogApi bizLogApi = new BizLogApi();
                bizLogApi.setBizId(id);
                bizLogApi.setOptType(OptTypeEnum.DELETE.getType());
                bizLogApi.setBizType(BizTypeEnum.AD_GROUP.getType());
                bizLogApi.setTitle("Ad Group Delete");
                bizLogApi.setContent("Status: " + BaseStatusEnum.of(beStatusMap.get(id).getStatus()).getDesc() + " -> " + BaseStatusEnum.DELETE.getDesc());
                bizLogApi.setCreator("admin");
                return bizLogApi;
            }).collect(Collectors.toList());
            saveBatch(bizLogApis);
        });
    }

    @Override
    public void logByUpdateAdGroupDirect(AdGroupVO beforeVO, AdGroupVO afterVO) {
        threadPool.execute(() -> {
            StringBuilder sb = new StringBuilder();
            try {
                compareObj(sb, beforeVO, afterVO);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (sb.length() > 0) {
                BizLogApi bizLogApi = new BizLogApi();
                bizLogApi.setBizId(beforeVO.getId());
                bizLogApi.setOptType(OptTypeEnum.UPDATE.getType());
                bizLogApi.setBizType(BizTypeEnum.AD_GROUP.getType());
                bizLogApi.setTitle("Ad Group Update Directly");
                bizLogApi.setContent(sb.substring(0, sb.length() - 1));
                bizLogApi.setCreator("admin");
                save(bizLogApi);
            }
        });
    }

    @Override
    public void logByUpdateAdGroupBundle(List<TargetCondition> befores, List<TargetCondition> afters) {
        Map<String, TargetCondition> beforeMap = befores.stream()
                .collect(Collectors.toMap(TargetCondition::getAttribute, Function.identity()));
        Map<String, TargetCondition> afterMap = afters.stream()
                .collect(Collectors.toMap(TargetCondition::getAttribute, Function.identity()));

        threadPool.execute(() -> {
            // 获取全部attributes
            Set<String> attributes = afterMap.keySet();
            int adGroupId = afters.get(0).getAdGroupId();
            for (String bundleAttr : attributes) {
                String title = bundleAttr.equals(ConditionEnum.AUTO_BUNDLE_EXCEPT.getDesc())
                        ? "Ad Group Auto Update Bundle"
                        : "Ad Group Update Bundle";
                BizLogApi bizLogApi = new BizLogApi();
                bizLogApi.setBizId(adGroupId);
                bizLogApi.setOptType(OptTypeEnum.UPDATE.getType());
                bizLogApi.setBizType(BizTypeEnum.AD_GROUP.getType());
                bizLogApi.setTitle(title);
                TargetCondition afterBundleCond = afterMap.get(bundleAttr);
                if (beforeMap.isEmpty() || beforeMap.get(bundleAttr) == null || StrUtil.isBlank(beforeMap.get(bundleAttr).getValue())) {
                    if (StrUtil.isNotBlank(afterBundleCond.getValue())) {
                        bizLogApi.setContent("Bundle: null -> " + afterBundleCond.getOperation() + " " + afterBundleCond.getValue());
                    }
                } else {
                    if (StrUtil.isBlank(afterBundleCond.getValue())) {
                        bizLogApi.setContent("Bundle: " + beforeMap.get(bundleAttr).getOperation() + " " + beforeMap.get(bundleAttr).getValue() + " -> null");
                    } else {
                        bizLogApi.setContent("Bundle: " + beforeMap.get(bundleAttr).getOperation() + " " + beforeMap.get(bundleAttr).getValue()
                                + " -> " + afterBundleCond.getOperation() + " " + afterBundleCond.getValue());
                    }
                }
                bizLogApi.setCreator("admin");
                if (StrUtil.isNotBlank(bizLogApi.getContent())) {
                    save(bizLogApi);
                }
            }
        });
    }

    @Override
    public void logByUpdateBatchAdGroup(Map<Integer, AdGroup> beAdGroupMap, BatchAdGroupUpdateVO afterVO) {
        threadPool.execute(() -> {
            List<BizLogApi> bizLogApis = afterVO.getAdGroupIds().stream().map(id -> {
                StringBuilder sb = new StringBuilder();
                try {
                    BatchAdGroupUpdateVO beforeVO = new BatchAdGroupUpdateVO();
                    BeanUtils.copyProperties(beAdGroupMap.get(id), beforeVO);
                    compareObj(sb, beforeVO, afterVO);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                BizLogApi bizLogApi = new BizLogApi();
                bizLogApi.setBizId(id);
                bizLogApi.setOptType(OptTypeEnum.UPDATE.getType());
                bizLogApi.setBizType(BizTypeEnum.AD_GROUP.getType());
                bizLogApi.setTitle("Ad Group Batch Update Bid Price / Bid Strategy / Budget / Status");
                bizLogApi.setCreator("admin");
                bizLogApi.setContent(sb.length() > 0 ? sb.substring(0, sb.length() - 1) : null);
                return bizLogApi;
            }).collect(Collectors.toList());
            bizLogApis = bizLogApis.stream().filter(e -> e.getContent() != null).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(bizLogApis)) {
                saveBatch(bizLogApis);
            }
        });
    }

    @Override
    public void logByUpdateBatchCondition(String attribute, Map<Integer, TargetCondition> beConditonMap,
                                          BundleAdGroupUpdateVO afterVO) {
        threadPool.execute(() -> {
            List<BizLogApi> bizLogApis = afterVO.getAdGroupIds().stream().map(id -> {
                StringBuilder sb = new StringBuilder();
                BizLogApi bizLogApi = new BizLogApi();
                bizLogApi.setBizId(id);
                bizLogApi.setOptType(OptTypeEnum.UPDATE.getType());
                bizLogApi.setBizType(BizTypeEnum.AD_GROUP.getType());
                bizLogApi.setTitle("Ad Group Batch Update " + attribute);

                TargetCondition beCondition = beConditonMap.get(id);
                if (StrUtil.isBlank(afterVO.getValue())) {
                    if (beConditonMap.containsKey(id)) {
                        sb.append(attribute).append(": ").append(beCondition.getOperation()).append(" ").append(beCondition.getValue()).append(" -> null");
                    }
                } else {
                    if (beConditonMap.containsKey(id)) {
                        if (beCondition.getOperation().equals(afterVO.getOperation())) {
                            if (!beCondition.getValue().equals(afterVO.getValue())) {
                                sb.append(attribute).append(": ").append(beCondition.getOperation()).append(" ")
                                        .append(beCondition.getValue()).append(" -> ").append(afterVO.getOperation()).append(" ").append(afterVO.getValue()).append("\n");
                            }
                        } else {
                            sb.append(attribute).append(": ").append(beCondition.getOperation()).append(" ")
                                    .append(beCondition.getValue()).append(" -> ").append(afterVO.getOperation()).append(" ").append(afterVO.getValue()).append("\n");
                        }
                    } else {
                        sb.append(attribute).append(": null -> ").append(afterVO.getOperation()).append(" ").append(afterVO.getValue());
                    }
                }
                bizLogApi.setCreator("admin");
                bizLogApi.setContent(sb.length() > 0 ? sb.substring(0, sb.length() - 1) : null);
                return bizLogApi;
            }).collect(Collectors.toList());
            bizLogApis = bizLogApis.stream().filter(e -> e.getContent() != null).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(bizLogApis)) {
                saveBatch(bizLogApis);
            }
        });
    }

    // =====================================================================================================
    @Override
    public void logByUpdateCampaign(CampaignVO beforeVO, CampaignVO afterVO) {
        threadPool.execute(() -> {
            StringBuilder sb = new StringBuilder();
            try {
                compareObj(sb, beforeVO, afterVO);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            CampaignRtaVO campaignRtaVO = beforeVO.getCampaignRtaVO();
            CampaignRtaVO afterCampaignRta = afterVO.getCampaignRtaVO();
            if (!(campaignRtaVO == null && afterCampaignRta == null)) {
                if (campaignRtaVO == null) {
                    campaignRtaVO = new CampaignRtaVO();
                }
                if (afterCampaignRta == null) {
                    afterCampaignRta = new CampaignRtaVO();
                }
                String advCampaignId = campaignRtaVO.getAdvCampaignId();
                Integer advMemId = campaignRtaVO.getAdvMemId();
                Integer rtaFeature = campaignRtaVO.getRtaFeature();
                String channel = campaignRtaVO.getChannel();
                if (StrUtil.isNotBlank(afterCampaignRta.getAdvCampaignId()) && !afterCampaignRta.getAdvCampaignId().equals(advCampaignId)) {
                    sb.append("Adv Campaign Id: ").append(advCampaignId).append(" -> ").append(afterCampaignRta.getAdvCampaignId()).append("\n");
                }
                if (afterCampaignRta.getAdvMemId() != null && !afterCampaignRta.getAdvMemId().equals(advMemId)) {
                    sb.append("Adv Mem Id: ").append(advMemId).append(" -> ").append(afterCampaignRta.getAdvMemId()).append("\n");
                }
                if (afterCampaignRta.getRtaFeature() != null && !afterCampaignRta.getRtaFeature().equals(rtaFeature)) {
                    sb.append("Rta Feature: ").append(rtaFeature).append(" -> ").append(afterCampaignRta.getRtaFeature()).append("\n");
                }
                if (StrUtil.isNotBlank(afterCampaignRta.getChannel()) && !afterCampaignRta.getChannel().equals(channel)) {
                    sb.append("AE Channel: ").append(channel).append(" -> ").append(afterCampaignRta.getChannel()).append("\n");
                }
            }
            if (sb.length() > 0) {
                BizLogApi bizLogApi = new BizLogApi();
                bizLogApi.setBizId(beforeVO.getId());
                bizLogApi.setOptType(OptTypeEnum.UPDATE.getType());
                bizLogApi.setBizType(BizTypeEnum.CAMPAIGN.getType());
                bizLogApi.setTitle("Campaign Update");
                bizLogApi.setContent(sb.substring(0, sb.length() - 1));
                bizLogApi.setCreator("admin");
                save(bizLogApi);
            }
        });
    }

    @Override
    public void logByDeleteCampaign(List<Integer> ids, List<StatusEntity> campaignStatusList) {
        Map<Integer, StatusEntity> beStatusMap = campaignStatusList.stream().collect(Collectors.toMap(IdEntity::getId, Function.identity()));
        threadPool.execute(() -> {
            List<BizLogApi> bizLogApis = ids.stream().map(id -> {
                BizLogApi bizLogApi = new BizLogApi();
                bizLogApi.setBizId(id);
                bizLogApi.setOptType(OptTypeEnum.DELETE.getType());
                bizLogApi.setBizType(BizTypeEnum.CAMPAIGN.getType());
                bizLogApi.setTitle("Campaign Delete");
                bizLogApi.setContent("Status: " + BaseStatusEnum.of(beStatusMap.get(id).getStatus()).getDesc() + " -> " + BaseStatusEnum.DELETE.getDesc());
                bizLogApi.setCreator("admin");
                return bizLogApi;
            }).collect(Collectors.toList());
            saveBatch(bizLogApis);
        });
    }

    @Override
    public void logByUpdateCampaignDirect(CampaignVO beforeVO, CampaignVO afterVO) {
        threadPool.execute(() -> {
            StringBuilder sb = new StringBuilder();
            try {
                compareObj(sb, beforeVO, afterVO);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (sb.length() > 0) {
                BizLogApi bizLogApi = new BizLogApi();
                bizLogApi.setBizId(beforeVO.getId());
                bizLogApi.setOptType(OptTypeEnum.UPDATE.getType());
                bizLogApi.setBizType(BizTypeEnum.CAMPAIGN.getType());
                bizLogApi.setTitle("Campaign Update Directly");
                bizLogApi.setContent(sb.substring(0, sb.length() - 1));
                bizLogApi.setCreator("admin");
                save(bizLogApi);
            }
        });
    }

    // ===============================================================================================
    @Override
    public void logByUpdateAd(AdVO beforeVO, AdVO afterVO) {
        threadPool.execute(() -> {
            StringBuilder sb = new StringBuilder();
            try {
                compareObj(sb, beforeVO, afterVO);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (sb.length() > 0) {
                BizLogApi bizLogApi = new BizLogApi();
                bizLogApi.setBizId(beforeVO.getId());
                bizLogApi.setOptType(OptTypeEnum.UPDATE.getType());
                bizLogApi.setBizType(BizTypeEnum.AD.getType());
                bizLogApi.setTitle("Ad Update");
                bizLogApi.setContent(sb.substring(0, sb.length() - 1));
                bizLogApi.setCreator("admin");
                save(bizLogApi);
            }
        });
    }

    @Override
    public void logByDeleteAd(List<Integer> ids, List<StatusEntity> adStatusList) {
        Map<Integer, StatusEntity> beStatusMap = adStatusList.stream().collect(Collectors.toMap(IdEntity::getId, Function.identity()));
        threadPool.execute(() -> {
            List<BizLogApi> bizLogApis = ids.stream().map(id -> {
                BizLogApi bizLogApi = new BizLogApi();
                bizLogApi.setBizId(id);
                bizLogApi.setOptType(OptTypeEnum.DELETE.getType());
                bizLogApi.setBizType(BizTypeEnum.AD.getType());
                bizLogApi.setTitle("Ad Delete");
                bizLogApi.setContent("Status: " + BaseStatusEnum.of(beStatusMap.get(id).getStatus()).getDesc() + " -> " + BaseStatusEnum.DELETE.getDesc());
                bizLogApi.setCreator("admin");
                return bizLogApi;
            }).collect(Collectors.toList());
            saveBatch(bizLogApis);
        });
    }

    @Override
    public void logByUpdateBatchAd(Map<Integer, Ad> beAdMap, BatchAdUpdateVO afterVO) {
        threadPool.execute(() -> {
            List<BizLogApi> bizLogApis = afterVO.getAdIds().stream().map(id -> {
                StringBuilder sb = new StringBuilder();
                try {
                    BatchAdUpdateVO beforeVO = new BatchAdUpdateVO();
                    BeanUtils.copyProperties(beAdMap.get(id), beforeVO);
                    compareObj(sb, beforeVO, afterVO);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                BizLogApi bizLogApi = new BizLogApi();
                bizLogApi.setBizId(id);
                bizLogApi.setOptType(OptTypeEnum.UPDATE.getType());
                bizLogApi.setBizType(BizTypeEnum.AD_GROUP.getType());
                bizLogApi.setTitle("Ad Batch Update Title / Description / CTA / Status");
                bizLogApi.setCreator("admin");
                bizLogApi.setContent(sb.length() > 0 ? sb.substring(0, sb.length() - 1) : null);
                return bizLogApi;
            }).collect(Collectors.toList());
            bizLogApis = bizLogApis.stream().filter(e -> e.getContent() != null).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(bizLogApis)) {
                saveBatch(bizLogApis);
            }
        });
    }

}
