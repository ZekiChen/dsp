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
        Long requestId = params.get(ParamKey.REQUEST_ID);

        doRta(doRtaByLazada(afterPriceFilterAdMap, bidRequest),
                params, ParamKey.REQUEST_LAZADA_RTA_RESPONSE, "lazada", requestId);
        doRta(doRtaByAE(afterPriceFilterAdMap, bidRequest),
                params, ParamKey.REQUEST_AE_RTA_RESPONSE, "ae", requestId);
        doRta(doRtaByMiravia(afterPriceFilterAdMap, bidRequest),
                params, ParamKey.REQUEST_MIRAVIA_RTA_RESPONSE, "miravia", requestId);

        log.info("contextId: {}, request rta", requestId);
    }

    private void doRta(Map<Integer, Target> afterRtaAdMap, Params params, String paramKey, String advName, Long requestId) {
        threadPool.execute(() -> {
            try {
                messageQueue.putMessage(EventType.REQUEST_RTA_RESPONSE, params.put(paramKey, afterRtaAdMap));
            } catch (Exception e) {
                log.error("contextId: {}, request {} rta cause a exception:", requestId, advName, e);
                messageQueue.putMessage(EventType.WAIT_REQUEST_RTA_RESPONSE_ERROR, params);
            }
        });
    }

    private Map<Integer, Target> doRtaByLazada(Map<Integer, AdDTOWrapper> afterPriceFilterAdMap,
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

    private Map<Integer, Target> doRtaByMiravia(Map<Integer, AdDTOWrapper> afterPriceFilterAdMap,
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

    private Map<Integer, Target> doRtaByAE(Map<Integer, AdDTOWrapper> afterPriceFilterAdMap,
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
