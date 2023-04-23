package com.tecdo.service.init;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tecdo.adm.api.delivery.dto.AdGroupDTO;
import com.tecdo.adm.api.delivery.dto.CampaignDTO;
import com.tecdo.adm.api.delivery.entity.*;
import com.tecdo.adm.api.delivery.mapper.*;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.starter.mp.entity.IdEntity;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2022/12/27
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class AdManager {

    private final SoftTimer softTimer;
    private final MessageQueue messageQueue;
    private final ThreadPool threadPool;

    private final AdMapper adMapper;
    private final CreativeMapper creativeMapper;
    private final AdGroupMapper adGroupMapper;
    private final TargetConditionMapper conditionMapper;
    private final CampaignMapper campaignMapper;
    private final CampaignRtaInfoMapper campaignRtaMapper;
    private final AdvMapper advMapper;

    private State currentState = State.INIT;
    private long timerId;

    private Map<Integer, AdDTO> adDTOMap;
    private Map<Integer, CampaignDTO> campaignDTOMap;

    @Value("${pac.timeout.load.db.ad-dto}")
    private long loadTimeout;
    @Value("${pac.interval.reload.db.default}")
    private long reloadInterval;

    /**
     * 从 DB 加载 ad（creative）- group（target_condition）- campaign（campaign_rta_info） 集合，每 5 分钟刷新一次缓存
     */
    public Map<Integer, AdDTO> getAdDTOMap() {
        return this.adDTOMap;
    }

    /**
     * 聚合 ad 数据至 CampaignDTO 集合，每 5 分钟刷新一次缓存
     */
    public Map<Integer, CampaignDTO> getCampaignDTOMap() {
        return this.campaignDTOMap;
    }

    @AllArgsConstructor
    private enum State {
        INIT(1, "init"),
        WAIT_INIT_RESPONSE(2, "waiting init response"),
        RUNNING(3, "init success, now is running"),
        UPDATING(4, "updating");

        private int code;
        private String desc;

        @Override
        public String toString() {
            return code + " - " + desc;
        }
    }

    public void init(Params params) {
        messageQueue.putMessage(EventType.ADS_LOAD, params);
    }

    private void startReloadTimeoutTimer(Params params) {
        timerId = softTimer.startTimer(EventType.ADS_LOAD_TIMEOUT, params, loadTimeout);
    }

    private void cancelReloadTimeoutTimer() {
        softTimer.cancel(timerId);
    }

    private void startNextReloadTimer(Params params) {
        softTimer.startTimer(EventType.ADS_LOAD, params, reloadInterval);
    }

    public void switchState(State state) {
        this.currentState = state;
    }

    public void handleEvent(EventType eventType, Params params) {
        switch (eventType) {
            case ADS_LOAD:
                handleAdsReload(params);
                break;
            case ADS_LOAD_RESPONSE:
                handleAdsResponse(params);
                break;
            case ADS_LOAD_ERROR:
                handleAdsError(params);
                break;
            case ADS_LOAD_TIMEOUT:
                handleAdsTimeout(params);
                break;
            default:
                log.error("Can't handle event, type: {}", eventType);
        }
    }

    private void handleAdsReload(Params params) {
        switch (currentState) {
            case INIT:
            case RUNNING:
                threadPool.execute(() -> {
                    try {
                        List<Ad> ads = adMapper.selectList(Wrappers.lambdaQuery());
                        Map<Integer, Creative> creativeMap = creativeMapper.selectList(Wrappers.lambdaQuery()).stream().collect(Collectors.toMap(IdEntity::getId, e -> e));
                        Map<Integer, AdGroup> adGroupMap = adGroupMapper.selectList(Wrappers.lambdaQuery()).stream().collect(Collectors.toMap(IdEntity::getId, e -> e));
                        List<TargetCondition> conditions = conditionMapper.selectList(Wrappers.lambdaQuery());
                        Map<Integer, Campaign> campaignMap = campaignMapper.selectList(Wrappers.lambdaQuery()).stream().collect(Collectors.toMap(IdEntity::getId, e -> e));
                        List<CampaignRtaInfo> campaignRtaInfos = campaignRtaMapper.selectList(Wrappers.lambdaQuery());

                        Map<Integer, AdDTO> adDTOMap = listAndConvertAds(ads, creativeMap, adGroupMap, conditions, campaignMap, campaignRtaInfos);
                        Map<Integer, CampaignDTO> campaignDTOMap = listCampaignDTOMap(ads, creativeMap, adGroupMap, conditions, campaignMap, campaignRtaInfos);

                        params.put(ParamKey.ADS_CACHE_KEY, adDTOMap);
                        params.put(ParamKey.CAMPAIGNS_CACHE_KEY, campaignDTOMap);
                        messageQueue.putMessage(EventType.ADS_LOAD_RESPONSE, params);
                    } catch (Exception e) {
                        log.error("ad list load failure from db", e);
                        messageQueue.putMessage(EventType.ADS_LOAD_ERROR, params);
                    }
                });
                startReloadTimeoutTimer(params);
                switchState(currentState == State.INIT ? State.WAIT_INIT_RESPONSE : State.UPDATING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    /**
     * 将 ad（creative）、ad_group（target_condition）、campaign（campaign_rta_info）数据平铺整合 AdDTO 集
     */
    private Map<Integer, AdDTO> listAndConvertAds(List<Ad> ads,
                                                  Map<Integer, Creative> creativeMap,
                                                  Map<Integer, AdGroup> adGroupMap,
                                                  List<TargetCondition> conditions,
                                                  Map<Integer, Campaign> campaignMap,
                                                  List<CampaignRtaInfo> campaignRtaInfos) {
        List<AdDTO> adDTOs = new ArrayList<>();
        for (Ad ad : ads) {
            Map<Integer, Creative> creatives = listCreativesByAd(creativeMap, ad);
            AdGroup adGroup = getAdGroupByAd(adGroupMap, ad);
            List<TargetCondition> targetConditions = listConditionByGroup(conditions, adGroup);
            Campaign campaign = getCampaignByGroup(campaignMap, adGroup);
            CampaignRtaInfo campaignRtaInfo = getCampaignRtaByCampaign(campaignRtaInfos, campaign);
            if(adGroup == null || campaign == null){
                continue;
            }
            AdDTO adDTO = AdDTO.builder()
                    .ad(ad).creativeMap(creatives)
                    .adGroup(adGroup).conditions(targetConditions)
                    .campaign(campaign).campaignRtaInfo(campaignRtaInfo)
                    .build();
            adDTOs.add(adDTO);
        }
        return adDTOs.stream().collect(Collectors.toMap(e -> e.getAd().getId(), e -> e));
    }

    private Map<Integer, CampaignDTO> listCampaignDTOMap(List<Ad> ads,
                                                         Map<Integer, Creative> creativeMap,
                                                         Map<Integer, AdGroup> adGroupMap,
                                                         List<TargetCondition> conditions,
                                                         Map<Integer, Campaign> campaignMap,
                                                         List<CampaignRtaInfo> campaignRtaInfos) {
        Map<Integer, Adv> advMap = advMapper.selectList(Wrappers.query()).stream().collect(Collectors.toMap(Adv::getId, e -> e));
        Map<Integer, CampaignDTO> campaignDTOMap = new HashMap<>();
        for (Campaign campaign : campaignMap.values()) {
            CampaignDTO campaignDTO = BeanUtil.copyProperties(campaign, CampaignDTO.class);
            if (campaignDTO.getAdvId() != null) {
                campaignDTO.setAdvName(advMap.get(campaignDTO.getAdvId()).getName());
            }
            List<AdGroupDTO> adGroupDTOs = adGroupMap.values().stream()
                    .filter(e -> Objects.equals(campaignDTO.getId(), e.getCampaignId()))
                    .map(e -> BeanUtil.copyProperties(e, AdGroupDTO.class))
                    .collect(Collectors.toList());
            campaignDTO.setAdGroupDTOs(adGroupDTOs);
            CampaignRtaInfo campaignRtaInfo = campaignRtaInfos.stream()
                    .filter(e -> Objects.equals(campaignDTO.getId(), e.getCampaignId()))
                    .findFirst().orElse(null);
            campaignDTO.setCampaignRtaInfo(campaignRtaInfo);
            campaignDTOMap.put(campaignDTO.getId(), campaignDTO);
        }
        return campaignDTOMap;
    }

    private Map<Integer, Creative> listCreativesByAd(Map<Integer, Creative> creativeMap, Ad ad) {
        Map<Integer, Creative> resMap = new HashMap<>();
        if (ad.getIcon() != null) {
            resMap.put(ad.getIcon(), creativeMap.get(ad.getIcon()));
        }
        if (ad.getImage() != null) {
            resMap.put(ad.getImage(), creativeMap.get(ad.getImage()));
        }
        if (ad.getVideo() != null) {
            resMap.put(ad.getVideo(), creativeMap.get(ad.getVideo()));
        }
        return resMap;
    }

    private AdGroup getAdGroupByAd(Map<Integer, AdGroup> adGroupMap, Ad ad) {
        return adGroupMap.get(ad.getGroupId());
    }

    private List<TargetCondition> listConditionByGroup(List<TargetCondition> conditions, AdGroup adGroup) {
        return adGroup == null ? null :
                conditions.stream().filter(e -> Objects.equals(adGroup.getId(), e.getAdGroupId())
                        && StrUtil.isAllNotBlank(e.getAttribute(), e.getOperation(), e.getValue())
                ).collect(Collectors.toList());
    }

    private Campaign getCampaignByGroup(Map<Integer, Campaign> campaignMap, AdGroup adGroup) {
        return adGroup == null ? null : campaignMap.get(adGroup.getCampaignId());
    }

    private CampaignRtaInfo getCampaignRtaByCampaign(List<CampaignRtaInfo> campaignRtaInfos, Campaign campaign) {
        if (campaign != null) {
            Optional<CampaignRtaInfo> optional = campaignRtaInfos.stream().filter(e -> Objects.equals(campaign.getId(), e.getCampaignId())).findFirst();
            if (optional.isPresent()) {
                return optional.get();
            }
        }
        return null;
    }

    private void handleAdsResponse(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
                messageQueue.putMessage(EventType.ONE_DATA_READY);
            case UPDATING:
                cancelReloadTimeoutTimer();
                this.adDTOMap = params.get(ParamKey.ADS_CACHE_KEY);
                this.campaignDTOMap = params.get(ParamKey.CAMPAIGNS_CACHE_KEY);
                log.info("ad dto list load success, size: {}, campaign dto list load success, size: {}"
                        , adDTOMap.size(), campaignDTOMap.size());
                startNextReloadTimer(params);
                switchState(State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleAdsError(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
            case UPDATING:
                cancelReloadTimeoutTimer();
                startNextReloadTimer(params);
                switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleAdsTimeout(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
            case UPDATING:
                startNextReloadTimer(params);
                switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }
}
