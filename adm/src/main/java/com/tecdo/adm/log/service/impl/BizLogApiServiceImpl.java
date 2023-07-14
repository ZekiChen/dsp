package com.tecdo.adm.log.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.entity.AdGroup;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.BidStrategyEnum;
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
            Boolean beBundleTestEnable = beforeVO.getBundleTestEnable();
            Boolean beForceJumpEnable = beforeVO.getForceJumpEnable();
            Double beForceJumpRatio = beforeVO.getForceJumpRatio();
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
            if (afterVO.getBundleTestEnable() != null && !afterVO.getBundleTestEnable() == beBundleTestEnable) {
                sb.append("Bundle Test Enable: ").append(beBundleTestEnable).append(" -> ").append(afterVO.getBundleTestEnable()).append("\n");
            }
            if (afterVO.getForceJumpEnable() != null && !afterVO.getForceJumpEnable() == beForceJumpEnable) {
                sb.append("Force Jump Enable: ").append(beForceJumpEnable).append(" -> ").append(afterVO.getForceJumpEnable()).append("\n");
            }
            if (afterVO.getForceJumpRatio() != null && !afterVO.getForceJumpRatio().equals(beForceJumpRatio)) {
                sb.append("Force Jump Ratio: ").append(beForceJumpRatio).append(" -> ").append(afterVO.getForceJumpRatio()).append("\n");
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
    public void logByUpdateBatchAdGroup(Map<Integer, AdGroup> beAdGroupMap, BatchAdGroupUpdateVO afterVO) {
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

    // =====================================================================================================
    @Override
    public void logByUpdateCampaign(CampaignVO beforeVO, CampaignVO afterVO) {
        threadPool.execute(() -> {
            String advName = beforeVO.getAdvName();
            Integer advType = beforeVO.getAdvType();
            Integer advId = beforeVO.getAdvId();
            String name = beforeVO.getName();
            Double budget = beforeVO.getDailyBudget();
            String packageName = beforeVO.getPackageName();
            String category = beforeVO.getCategory();
            String domain = beforeVO.getDomain();
            String remark = beforeVO.getRemark();
            Integer status = beforeVO.getStatus();
            CampaignRtaVO campaignRtaVO = beforeVO.getCampaignRtaVO();

            StringBuilder sb = new StringBuilder();
            if (afterVO.getAdvId() != null && !afterVO.getAdvId().equals(advId)) {
                sb.append("Adv Name: ").append(advName).append(" -> ").append(afterVO.getAdvName()).append("\n");
            }
            if (afterVO.getAdvType() != null && !afterVO.getAdvType().equals(advType)) {
                sb.append("Adv Type: ").append(advType).append(" -> ").append(afterVO.getAdvType()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getName()) && !afterVO.getName().equals(name)) {
                sb.append("Campaign Name: ").append(name).append(" -> ").append(afterVO.getName()).append("\n");
            }
            if (afterVO.getDailyBudget() != null && !afterVO.getDailyBudget().equals(budget)) {
                sb.append("Budget: ").append(budget).append(" -> ").append(afterVO.getDailyBudget()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getPackageName()) && !afterVO.getPackageName().equals(packageName)) {
                sb.append("Bundle: ").append(packageName).append(" -> ").append(afterVO.getPackageName()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getCategory()) && !afterVO.getCategory().equals(category)) {
                sb.append("APP Categories: ").append(category).append(" -> ").append(afterVO.getCategory()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getDomain()) && !afterVO.getDomain().equals(domain)) {
                sb.append("Domain: ").append(domain).append(" -> ").append(afterVO.getDomain()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getRemark()) && !afterVO.getCategory().equals(remark)) {
                sb.append("Remark: ").append(remark).append(" -> ").append(afterVO.getRemark()).append("\n");
            }
            if (afterVO.getStatus() != null && !afterVO.getStatus().equals(status)) {
                sb.append("Status: ").append(BaseStatusEnum.of(status).getDesc()).append(" -> ")
                        .append(BaseStatusEnum.of(afterVO.getStatus()).getDesc()).append("\n");
            }
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
            String name = beforeVO.getName();
            Double budget = beforeVO.getDailyBudget();
            String remark = beforeVO.getRemark();
            Integer status = beforeVO.getStatus();

            StringBuilder sb = new StringBuilder();
            if (StrUtil.isNotBlank(afterVO.getName()) && !afterVO.getName().equals(name)) {
                sb.append("Campaign Name: ").append(name).append(" -> ").append(afterVO.getName()).append("\n");
            }
            if (afterVO.getDailyBudget() != null && !afterVO.getDailyBudget().equals(budget)) {
                sb.append("Budget: ").append(budget).append(" -> ").append(afterVO.getDailyBudget()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getRemark()) && !afterVO.getRemark().equals(remark)) {
                sb.append("Remark: ").append(remark).append(" -> ").append(afterVO.getRemark()).append("\n");
            }
            if (afterVO.getStatus() != null && !afterVO.getStatus().equals(status)) {
                sb.append("Status: ").append(BaseStatusEnum.of(status).getDesc()).append(" -> ")
                        .append(BaseStatusEnum.of(afterVO.getStatus()).getDesc()).append("\n");
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
            Integer groupId = beforeVO.getGroupId();
            String name = beforeVO.getName();
            Integer type = beforeVO.getType();
            Integer image = beforeVO.getImage();
            Integer icon = beforeVO.getIcon();
            Integer video = beforeVO.getVideo();
//            String imageUrl = beforeVO.getImageUrl();
//            String iconUrl = beforeVO.getIconUrl();
//            String videoUrl = beforeVO.getVideoUrl();
            String title = beforeVO.getTitle();
            String description = beforeVO.getDescription();
            String cta = beforeVO.getCta();
            String remark = beforeVO.getRemark();
            Integer status = beforeVO.getStatus();

            StringBuilder sb = new StringBuilder();
            if (afterVO.getGroupId() != null && !afterVO.getGroupId().equals(groupId)) {
                sb.append("Ad Group Id: ").append(groupId).append(" -> ").append(afterVO.getGroupId()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getName()) && !afterVO.getName().equals(name)) {
                sb.append("Ad Name: ").append(name).append(" -> ").append(afterVO.getName()).append("\n");
            }
            if (afterVO.getType() != null && !afterVO.getType().equals(type)) {
                sb.append("Ad Format: ").append(type).append(" -> ").append(afterVO.getType()).append("\n");
            }
            if (afterVO.getImage() != null && !afterVO.getImage().equals(image)) {
                sb.append("Image Creative Id: ").append(image).append(" -> ").append(afterVO.getImage()).append("\n");
            }
            if (afterVO.getIcon() != null && !afterVO.getIcon().equals(icon)) {
                sb.append("Icon Creative Id: ").append(icon).append(" -> ").append(afterVO.getIcon()).append("\n");
            }
            if (afterVO.getVideo() != null && !afterVO.getVideo().equals(video)) {
                sb.append("Video Creative Id: ").append(video).append(" -> ").append(afterVO.getVideo()).append("\n");
            }
//            if (StrUtil.isNotBlank(afterVO.getImageUrl()) && !afterVO.getImageUrl().equals(imageUrl)) {
//                sb.append("Image Url: ").append(imageUrl).append(" -> ").append(afterVO.getImageUrl()).append("\n");
//            }
//            if (StrUtil.isNotBlank(afterVO.getIconUrl()) && !afterVO.getName().equals(iconUrl)) {
//                sb.append("Icon Url: ").append(iconUrl).append(" -> ").append(afterVO.getIconUrl()).append("\n");
//            }
//            if (StrUtil.isNotBlank(afterVO.getVideoUrl()) && !afterVO.getVideoUrl().equals(videoUrl)) {
//                sb.append("Video Url: ").append(videoUrl).append(" -> ").append(afterVO.getVideoUrl()).append("\n");
//            }
            if (StrUtil.isNotBlank(afterVO.getTitle()) && !afterVO.getTitle().equals(title)) {
                sb.append("Title: ").append(title).append(" -> ").append(afterVO.getTitle()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getDescription()) && !afterVO.getDescription().equals(description)) {
                sb.append("Description: ").append(description).append(" -> ").append(afterVO.getDescription()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getCta()) && !afterVO.getCta().equals(cta)) {
                sb.append("CTA: ").append(cta).append(" -> ").append(afterVO.getCta()).append("\n");
            }
            if (StrUtil.isNotBlank(afterVO.getRemark()) && !afterVO.getRemark().equals(remark)) {
                sb.append("Remark: ").append(remark).append(" -> ").append(afterVO.getRemark()).append("\n");
            }
            if (afterVO.getStatus() != null && !afterVO.getStatus().equals(status)) {
                sb.append("Status: ").append(BaseStatusEnum.of(status).getDesc()).append(" -> ")
                        .append(BaseStatusEnum.of(afterVO.getStatus()).getDesc()).append("\n");
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
                BizLogApi bizLogApi = new BizLogApi();
                bizLogApi.setBizId(id);
                bizLogApi.setOptType(OptTypeEnum.UPDATE.getType());
                bizLogApi.setBizType(BizTypeEnum.AD_GROUP.getType());
                bizLogApi.setTitle("Ad Batch Update Title / Description / CTA / Status");
                if (afterVO.getTitle() != null) {
                    sb.append("Title: ").append(beAdMap.get(id).getTitle()).append(" -> ").append(afterVO.getTitle()).append("\n");
                }
                if (afterVO.getDescription() != null) {
                    sb.append("Description: ").append(beAdMap.get(id).getDescription()).append(" -> ").append(afterVO.getDescription()).append("\n");
                }
                if (afterVO.getCta() != null) {
                    sb.append("CTA: ").append(beAdMap.get(id).getCta()).append(" -> ").append(afterVO.getCta()).append("\n");
                }
                if (afterVO.getStatus() != null) {
                    sb.append("Status: ").append(BaseStatusEnum.of(beAdMap.get(id).getStatus()).getDesc()).append(" -> ")
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

}
