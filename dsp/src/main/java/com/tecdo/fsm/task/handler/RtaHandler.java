package com.tecdo.fsm.task.handler;

import com.tecdo.adm.api.delivery.entity.RtaInfo;
import com.tecdo.adm.api.delivery.enums.AdvTypeEnum;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.service.CacheService;
import com.tecdo.service.init.RtaInfoManager;
import com.tecdo.service.rta.RtaHelper;
import com.tecdo.service.rta.Target;
import com.tecdo.service.rta.ae.AeRtaInfoVO;
import com.tecdo.service.rta.miravia.MiraviaRtaHelper;
import com.tecdo.util.StringConfigUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RTA
 *
 * Created by Zeki on 2023/9/22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RtaHandler {

    private final ThreadPool threadPool;
    private final MessageQueue messageQueue;
    private final RtaInfoManager rtaInfoManager;
    private final CacheService cacheService;

    public void requestRta(Params params, Map<Integer, AdDTOWrapper> afterPriceFilterAdMap,
                           BidRequest bidRequest) {
        String taskId = params.get(ParamKey.TASK_ID);

        threadPool.execute(() -> {
            try {
                Map<Integer, Target> rtaResMap = doRequestRtaByLazada(afterPriceFilterAdMap, bidRequest);
                messageQueue.putMessage(EventType.REQUEST_RTA_RESPONSE,
                        params.put(ParamKey.REQUEST_LAZADA_RTA_RESPONSE, rtaResMap));
            } catch (Exception e) {
                log.error("taskId: {}, request lazada rta cause a exception:", taskId, e);
                messageQueue.putMessage(EventType.WAIT_REQUEST_RTA_RESPONSE_ERROR, params);
            }
        });

        threadPool.execute(() -> {
            try {
                Map<Integer, Target> rtaResMap = doRequestRtaByAE(afterPriceFilterAdMap, bidRequest);
                messageQueue.putMessage(EventType.REQUEST_RTA_RESPONSE,
                        params.put(ParamKey.REQUEST_AE_RTA_RESPONSE, rtaResMap));
            } catch (Exception e) {
                log.error("taskId: {}, request ae rta cause a exception:", taskId, e);
                messageQueue.putMessage(EventType.WAIT_REQUEST_RTA_RESPONSE_ERROR, params);
            }
        });

        threadPool.execute(() -> {
            try {
                Map<Integer, Target> rtaResMap = doRequestRtaByMiravia(afterPriceFilterAdMap, bidRequest);
                messageQueue.putMessage(EventType.REQUEST_RTA_RESPONSE,
                        params.put(ParamKey.REQUEST_MIRAVIA_RTA_RESPONSE, rtaResMap));
            } catch (Exception e) {
                log.error("taskId: {}, request miravia rta cause a exception:", taskId, e);
                messageQueue.putMessage(EventType.WAIT_REQUEST_RTA_RESPONSE_ERROR, params);
            }
        });

        log.info("taskId: {}, request rta", taskId);
    }

    private Map<Integer, Target> doRequestRtaByLazada(Map<Integer, AdDTOWrapper> afterPriceFilterAdMap,
                                                      BidRequest bidRequest) {
        // 协议中的是国家三字码，需要转为对应的二字码
        String country = bidRequest.getDevice().getGeo().getCountry();
        String countryCode = StringConfigUtil.getCountryCode2(country);
        String deviceId = bidRequest.getDevice().getIfa();
        Map<Integer, Target> rtaResMap = new HashMap<>();

        // 只保留lazada rta的单子，并将单子按照广告主分组
        Map<Integer, List<AdDTOWrapper>> advToAdList = //
                afterPriceFilterAdMap.values().stream()
                        .filter(i -> Objects.nonNull(i.getAdDTO().getCampaignRtaInfo()) &&
                                AdvTypeEnum.LAZADA_RTA.getType() ==
                                        i.getAdDTO().getAdv().getType())
                        .collect(Collectors.groupingBy(i -> i.getAdDTO()
                                .getCampaignRtaInfo()
                                .getAdvMemId()));
        // 分广告主进行rta匹配
        advToAdList.forEach((advId, adList) -> {
            RtaInfo rtaInfo = rtaInfoManager.getRtaInfo(advId);
            RtaHelper.requestRta(rtaInfo, adList, countryCode, deviceId, rtaResMap);
        });
        return rtaResMap;
    }

    private Map<Integer, Target> doRequestRtaByMiravia(Map<Integer, AdDTOWrapper> afterPriceFilterAdMap,
                                                       BidRequest bidRequest) {
        String country = bidRequest.getDevice().getGeo().getCountry();
        String countryCode = StringConfigUtil.getCountryCode2(country);
        String deviceId = bidRequest.getDevice().getIfa();
        Map<Integer, Target> rtaResMap = new HashMap<>();

        // 只保留miravia rta的单子，并将单子按照广告主分组
        Map<Integer, List<AdDTOWrapper>> advToAdList = //
                afterPriceFilterAdMap.values().stream()
                        .filter(i -> Objects.nonNull(i.getAdDTO().getCampaignRtaInfo()) &&
                                AdvTypeEnum.MIRAVIA_RTA.getType() ==
                                        i.getAdDTO().getAdv().getType())
                        .collect(Collectors.groupingBy(i -> i.getAdDTO()
                                .getCampaignRtaInfo()
                                .getAdvMemId()));
        // 分广告主进行rta匹配
        advToAdList.forEach((advId, adList) -> {
            RtaInfo rtaInfo = rtaInfoManager.getRtaInfo(advId);
            MiraviaRtaHelper.requestRta(rtaInfo, adList, countryCode, deviceId, rtaResMap);
        });
        return rtaResMap;
    }

    private Map<Integer, Target> doRequestRtaByAE(Map<Integer, AdDTOWrapper> afterPriceFilterAdMap,
                                                  BidRequest bidRequest) {
        String deviceId = bidRequest.getDevice().getIfa();
        // 只保留ae rta的单子
        Map<Integer, String> cid2AdvCid = //
                afterPriceFilterAdMap.values().stream()
                        .filter(i -> Objects.nonNull(i.getAdDTO().getCampaignRtaInfo()) &
                                AdvTypeEnum.AE_RTA.getType() ==
                                        i.getAdDTO().getAdv().getType())
                        .collect(Collectors.toMap(ad -> ad.getAdDTO().getCampaign().getId(),
                                ad -> ad.getAdDTO()
                                        .getCampaignRtaInfo()
                                        .getAdvCampaignId(),
                                (o, n) -> o));
        Set<String> advCampaignIds = new HashSet<>(cid2AdvCid.values());
        List<AeRtaInfoVO> aeRtaInfoVOs =
                cacheService.getRtaCache().getAeRtaResponse(advCampaignIds, deviceId);
        Map<String, AeRtaInfoVO> advCId2AeRtaVOMap =
                aeRtaInfoVOs.stream().collect(Collectors.toMap(AeRtaInfoVO::getAdvCampaignId, e -> e));
        return cid2AdvCid.entrySet().stream().map(entry -> {
            Integer campaignId = entry.getKey();
            String advCampaignId = entry.getValue();
            AeRtaInfoVO vo = advCId2AeRtaVOMap.get(advCampaignId);
            Target target = new Target();
            target.setAdvType(AdvTypeEnum.AE_RTA.getType());
            target.setTarget(vo.getTarget());
            target.setLandingPage(vo.getLandingPage());  // cache sink 已经处理过了，取该层即可
            target.setDeeplink(vo.getDeeplink());
            return new AbstractMap.SimpleEntry<>(campaignId, target);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
