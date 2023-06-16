package com.tecdo.adm.log.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.BidStrategyEnum;
import com.tecdo.adm.api.delivery.vo.AdGroupVO;
import com.tecdo.adm.api.delivery.vo.BatchAdGroupUpdateVO;
import com.tecdo.adm.api.delivery.vo.BundleAdGroupUpdateVO;
import com.tecdo.adm.api.delivery.vo.TargetConditionVO;
import com.tecdo.adm.api.log.entity.BizLogApi;
import com.tecdo.adm.api.log.enums.BizTypeEnum;
import com.tecdo.adm.api.log.enums.OptTypeEnum;
import com.tecdo.adm.api.log.mapper.BizLogApiMapper;
import com.tecdo.adm.log.service.IBizLogApiService;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.starter.mp.entity.IdEntity;
import com.tecdo.starter.mp.entity.StatusEntity;
import com.tecdo.starter.mp.enums.BaseStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
            Integer beCampaignId = beforeVO.getCampaignId();
            String beAdGroupName = beforeVO.getName();
            String beClickUrl = beforeVO.getClickUrl();
            String beDeeplink = beforeVO.getDeeplink();
            String beImpTrackUrls = beforeVO.getImpTrackUrls();
            String beClickTrackUrls = beforeVO.getClickTrackUrls();
            Double beBudget = beforeVO.getDailyBudget();
            Integer beBidStrategy = beforeVO.getBidStrategy();
            Double beOptPrice = beforeVO.getOptPrice();
            Double beBidMultiplier = beforeVO.getBidMultiplier();
            Double beBidProbability = beforeVO.getBidProbability();
            String beRemark = beforeVO.getRemark();
            Integer beStatus = beforeVO.getStatus();
            List<TargetConditionVO> beConditionVOs = beforeVO.getConditionVOs();

            StringBuilder sb = new StringBuilder();
            if (afterVO.getCampaignId() != null && !afterVO.getCampaignId().equals(beCampaignId)) {
                sb.append("Ad Group Id: ").append(beCampaignId).append(" -> ").append(afterVO.getCampaignId()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getName()) && !afterVO.getName().equals(beAdGroupName)) {
                sb.append("Ad Group Name: ").append(beAdGroupName).append(" -> ").append(afterVO.getName()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getClickUrl()) && !afterVO.getClickUrl().equals(beClickUrl)) {
                sb.append("Click Url: ").append(beClickUrl).append(" -> ").append(afterVO.getClickUrl()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getDeeplink()) && !afterVO.getDeeplink().equals(beDeeplink)) {
                sb.append("Deeplink: ").append(beDeeplink).append(" -> ").append(afterVO.getDeeplink()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getImpTrackUrls()) && !afterVO.getImpTrackUrls().equals(beImpTrackUrls)) {
                sb.append("Imp Tracking URL: ").append(beImpTrackUrls).append(" -> ").append(afterVO.getImpTrackUrls()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getClickTrackUrls()) && !afterVO.getClickTrackUrls().equals(beClickTrackUrls)) {
                sb.append("Click Tracking URL: ").append(beClickTrackUrls).append(" -> ").append(afterVO.getClickTrackUrls()).append("\n");
            }
            if (afterVO.getDailyBudget() != null && !afterVO.getDailyBudget().equals(beBudget)) {
                sb.append("Budget: ").append(beBudget).append(" -> ").append(afterVO.getDailyBudget()).append("\n");
            }
            if (afterVO.getBidStrategy() != null && !afterVO.getBidStrategy().equals(beBidStrategy)) {
                sb.append("Bid Strategy: ").append(BidStrategyEnum.of(beBidStrategy).getDesc()).append(" -> ")
                        .append(BidStrategyEnum.of(afterVO.getBidStrategy()).getDesc()).append("\n");
            }
            if (afterVO.getOptPrice() != null && !afterVO.getOptPrice().equals(beOptPrice)) {
                sb.append("Bid Price: ").append(beOptPrice).append(" -> ").append(afterVO.getOptPrice()).append("\n");
            }
            if (afterVO.getBidMultiplier() != null && !afterVO.getBidMultiplier().equals(beBidMultiplier)) {
                sb.append("Bid Multiplier: ").append(beBidMultiplier).append(" -> ").append(afterVO.getBidMultiplier()).append("\n");
            }
            if (afterVO.getBidProbability() != null && !afterVO.getBidProbability().equals(beBidProbability)) {
                sb.append("Bid Probability: ").append(beBidProbability).append(" -> ").append(afterVO.getBidProbability()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getRemark()) && !afterVO.getRemark().equals(beRemark)) {
                sb.append("Remark: ").append(beRemark).append(" -> ").append(afterVO.getRemark()).append("\n");
            }
            if (afterVO.getStatus() != null && !afterVO.getStatus().equals(beStatus)) {
                sb.append("Status: ").append(BaseStatusEnum.of(beStatus).getDesc()).append(" -> ")
                        .append(BaseStatusEnum.of(afterVO.getStatus()).getDesc()).append("\n");
            }
            List<TargetCondition> afterConditions = afterVO.listCondition();
            if (CollUtil.isNotEmpty(afterConditions)) {
                Map<String, TargetCondition> afterMap = afterConditions.stream().collect(Collectors.toMap(TargetCondition::getAttribute, v -> v));
                for (TargetConditionVO before : beConditionVOs) {
                    if (afterMap.containsKey(before.getAttribute())) {
                        TargetCondition after = afterMap.get(before.getAttribute());
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

    @Override
    public void logByDeleteAdGroup(List<Integer> ids, List<StatusEntity> adStatusList) {
        Map<Integer, StatusEntity> beAdStatusMap = adStatusList.stream().collect(Collectors.toMap(IdEntity::getId, Function.identity()));
        threadPool.execute(() -> {
            List<BizLogApi> bizLogApis = ids.stream().map(id -> {
                BizLogApi bizLogApi = new BizLogApi();
                bizLogApi.setBizId(id);
                bizLogApi.setOptType(OptTypeEnum.DELETE.getType());
                bizLogApi.setBizType(BizTypeEnum.AD_GROUP.getType());
                bizLogApi.setTitle("Ad Group Delete");
                bizLogApi.setContent("Status: " + BaseStatusEnum.of(beAdStatusMap.get(id).getStatus()).getDesc() + " -> " + BaseStatusEnum.DELETE.getDesc());
                bizLogApi.setCreator("admin");
                return bizLogApi;
            }).collect(Collectors.toList());
            saveBatch(bizLogApis);
        });
    }

    @Override
    public void logByUpdateAdGroupDirect(AdGroupVO beforeVO, AdGroupVO afterVO) {
        threadPool.execute(() -> {
            Integer beCampaignId = beforeVO.getCampaignId();
            String beAdGroupName = beforeVO.getName();
            String beClickUrl = beforeVO.getClickUrl();
            String beDeeplink = beforeVO.getDeeplink();
            String beImpTrackUrls = beforeVO.getImpTrackUrls();
            String beClickTrackUrls = beforeVO.getClickTrackUrls();
            Double beBudget = beforeVO.getDailyBudget();
            Integer beBidStrategy = beforeVO.getBidStrategy();
            Double beOptPrice = beforeVO.getOptPrice();
            Double beBidMultiplier = beforeVO.getBidMultiplier();
            Double beBidProbability = beforeVO.getBidProbability();
            String beRemark = beforeVO.getRemark();
            Integer beStatus = beforeVO.getStatus();

            StringBuilder sb = new StringBuilder();
            if (afterVO.getCampaignId() != null && !afterVO.getCampaignId().equals(beCampaignId)) {
                sb.append("Ad Group Id: ").append(beCampaignId).append(" -> ").append(afterVO.getCampaignId()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getName()) && !afterVO.getName().equals(beAdGroupName)) {
                sb.append("Ad Group Name: ").append(beAdGroupName).append(" -> ").append(afterVO.getName()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getClickUrl()) && !afterVO.getClickUrl().equals(beClickUrl)) {
                sb.append("Click Url: ").append(beClickUrl).append(" -> ").append(afterVO.getClickUrl()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getDeeplink()) && !afterVO.getDeeplink().equals(beDeeplink)) {
                sb.append("Deeplink: ").append(beDeeplink).append(" -> ").append(afterVO.getDeeplink()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getImpTrackUrls()) && !afterVO.getImpTrackUrls().equals(beImpTrackUrls)) {
                sb.append("Imp Tracking URL: ").append(beImpTrackUrls).append(" -> ").append(afterVO.getImpTrackUrls()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getClickTrackUrls()) && !afterVO.getClickTrackUrls().equals(beClickTrackUrls)) {
                sb.append("Click Tracking URL: ").append(beClickTrackUrls).append(" -> ").append(afterVO.getClickTrackUrls()).append("\n");
            }
            if (afterVO.getDailyBudget() != null && !afterVO.getDailyBudget().equals(beBudget)) {
                sb.append("Budget: ").append(beBudget).append(" -> ").append(afterVO.getDailyBudget()).append("\n");
            }
            if (afterVO.getBidStrategy() != null && !afterVO.getBidStrategy().equals(beBidStrategy)) {
                sb.append("Bid Strategy: ").append(BidStrategyEnum.of(beBidStrategy).getDesc()).append(" -> ")
                        .append(BidStrategyEnum.of(afterVO.getBidStrategy()).getDesc()).append("\n");
            }
            if (afterVO.getOptPrice() != null && !afterVO.getOptPrice().equals(beOptPrice)) {
                sb.append("Bid Price: ").append(beOptPrice).append(" -> ").append(afterVO.getOptPrice()).append("\n");
            }
            if (afterVO.getBidMultiplier() != null && !afterVO.getBidMultiplier().equals(beBidMultiplier)) {
                sb.append("Bid Multiplier: ").append(beBidMultiplier).append(" -> ").append(afterVO.getBidMultiplier()).append("\n");
            }
            if (afterVO.getBidProbability() != null && !afterVO.getBidProbability().equals(beBidProbability)) {
                sb.append("Bid Probability: ").append(beBidProbability).append(" -> ").append(afterVO.getBidProbability()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getRemark()) && !afterVO.getRemark().equals(beRemark)) {
                sb.append("Remark: ").append(beRemark).append(" -> ").append(afterVO.getRemark()).append("\n");
            }
            if (afterVO.getStatus() != null && !afterVO.getStatus().equals(beStatus)) {
                sb.append("Status: ").append(BaseStatusEnum.of(beStatus).getDesc()).append(" -> ")
                        .append(BaseStatusEnum.of(afterVO.getStatus()).getDesc()).append("\n");
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
    public void logByUpdateAdGroupBundle(TargetCondition before, TargetCondition after) {
        threadPool.execute(() -> {
            BizLogApi bizLogApi = new BizLogApi();
            bizLogApi.setBizId(after.getAdGroupId());
            bizLogApi.setOptType(OptTypeEnum.UPDATE.getType());
            bizLogApi.setBizType(BizTypeEnum.AD_GROUP.getType());
            bizLogApi.setTitle("Ad Group Update Bundle");
            if (before == null || StrUtil.isBlank(before.getValue())) {
                if (StrUtil.isNotBlank(after.getValue())) {
                    bizLogApi.setContent("Bundle: null -> " + after.getOperation() + " " + after.getValue());
                }
            } else {
                if (StrUtil.isBlank(after.getValue())) {
                    bizLogApi.setContent("Bundle: " + before.getOperation() + " " + before.getValue() + " -> null");
                } else {
                    bizLogApi.setContent("Bundle: " + before.getOperation() + " " + before.getValue()
                            + " -> " + after.getOperation() + " " + after.getValue());
                }
            }
            bizLogApi.setCreator("admin");
            if (StrUtil.isNotBlank(bizLogApi.getContent())) {
                save(bizLogApi);
            }
        });
    }

    @Override
    public void logByUpdateBatch(Map<Integer, AdGroup> beAdGroupMap, BatchAdGroupUpdateVO afterVO) {
        threadPool.execute(() -> {
            List<BizLogApi> bizLogApis = afterVO.getAdGroupIds().stream().map(id -> {
                StringBuilder sb = new StringBuilder();
                BizLogApi bizLogApi = new BizLogApi();
                bizLogApi.setBizId(id);
                bizLogApi.setOptType(OptTypeEnum.UPDATE.getType());
                bizLogApi.setBizType(BizTypeEnum.AD_GROUP.getType());
                bizLogApi.setTitle("Ad Group Batch Update Bid Price / Bid Strategy / Budget / Status");
                if (afterVO.getOptPrice() != null) {
                    sb.append("Bid Price: ").append(beAdGroupMap.get(id).getOptPrice()).append(" -> ").append(afterVO.getOptPrice()).append("\n");
                }
                if (afterVO.getBidStrategy() != null) {
                    sb.append("Bid Strategy: ").append(beAdGroupMap.get(id).getBidStrategy()).append(" -> ").append(afterVO.getBidStrategy()).append("\n");
                }
                if (afterVO.getDailyBudget() != null) {
                    sb.append("Budget: ").append(beAdGroupMap.get(id).getDailyBudget()).append(" -> ").append(afterVO.getDailyBudget()).append("\n");
                }
                if (afterVO.getStatus() != null) {
                    sb.append("Status: ").append(BaseStatusEnum.of(beAdGroupMap.get(id).getStatus()).getDesc()).append(" -> ")
                            .append(BaseStatusEnum.of(afterVO.getStatus()).getDesc()).append("\n");
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
}
