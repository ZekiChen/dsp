package com.tecdo.fsm.task.handler;

import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.biz.dto.BidfloorDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.filter.AbstractRecallFilter;
import com.tecdo.filter.factory.RecallFiltersFactory;
import com.tecdo.filter.util.FilterChainHelper;
import com.tecdo.service.CacheService;
import com.tecdo.service.PmpService;
import com.tecdo.service.cache.FrequencyCache;
import com.tecdo.service.init.AdManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 广告召回
 * <p>
 * Created by Zeki on 2023/9/22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdRecallHandler {

    private final ThreadPool threadPool;
    private final MessageQueue messageQueue;
    private final RecallFiltersFactory filtersFactory;
    private final AdManager adManager;
    private final CacheService cacheService;
    private final PmpService pmpService;

    @Value("${pac.timeout.task.ad.recall}")
    private int recallTimeout;

    public void adRecall(Params params, BidRequest bidRequest, Imp imp,
                         Affiliate affiliate, boolean recallBatchEnable) {
        threadPool.execute(() -> {
            try {
                Map<Integer, AdDTOWrapper> res = recallBatchEnable
                        ? doAdRecallBatch(params, bidRequest, imp, affiliate)
                        : doAdRecall(params, bidRequest, imp, affiliate);
                params.put(ParamKey.ADS_RECALL_RESPONSE, res);
                messageQueue.putMessage(EventType.ADS_RECALL_FINISH, params);
            } catch (Exception e) {
                log.error("taskId: {},list recall ad error,  so this request will not participate in bidding",
                        params.get(ParamKey.TASK_ID), e);
                messageQueue.putMessage(EventType.ADS_RECALL_ERROR, params);
            }
        });
    }

    private Map<Integer, AdDTOWrapper> doAdRecall(Params params, BidRequest bidRequest,
                                                  Imp imp, Affiliate affiliate) {
        List<AbstractRecallFilter> filters = filtersFactory.createFilters();
        return adManager.getAdDTOMap()
                .values()
                .stream()
                .filter(adDTO -> FilterChainHelper.executeFilter(params.get(ParamKey.TASK_ID), filters.get(0), adDTO, bidRequest, imp, affiliate))
                .collect(Collectors.toMap(
                        adDTO -> adDTO.getAd().getId(),
                        adDTO -> buildADDTOWrapper(params.get(ParamKey.TASK_ID), bidRequest, imp.getId(), adDTO))
                );
    }

    private Map<Integer, AdDTOWrapper> doAdRecallBatch(Params params, BidRequest bidRequest,
                                                       Imp imp, Affiliate affiliate) {
        List<AbstractRecallFilter> filters = filtersFactory.createFilters();
        Map<Integer, AdDTO> adDTOMap = adManager.getAdDTOMap();
        List<CompletableFuture<AdDTOWrapper>> futureList = new ArrayList<>();
        Map<Integer, AdDTOWrapper> res = new HashMap<>();
        for (AdDTO adDTO : adDTOMap.values()) {
            // 在线程池中处理多个ad的recall
            CompletableFuture<AdDTOWrapper> future = CompletableFuture.supplyAsync(() ->
                            FilterChainHelper.executeFilter(params.get(ParamKey.TASK_ID), filters.get(0), adDTO, bidRequest, imp, affiliate)
                                    ? buildADDTOWrapper(params.get(ParamKey.TASK_ID), bidRequest, imp.getId(), adDTO)
                                    : null,
                    threadPool.getExecutor());
            futureList.add(future);
        }

        // 合并为一个future
        CompletableFuture<List<AdDTOWrapper>> total = sequence(futureList);
        try {
            List<AdDTOWrapper> adDTOWrappers = total.get(recallTimeout, TimeUnit.MILLISECONDS);
            boolean isPmpRequest = pmpService.isPmpRequest(imp);
            for (AdDTOWrapper wrapper : adDTOWrappers) {
                if (wrapper != null) {
                    // 获取ad对应的bidfloor
                    BidfloorDTO bidfloorDTO = isPmpRequest ?
                            pmpService.getBidfloor(wrapper.getAdDTO(), imp, affiliate.getId(), imp.getBidfloor())
                            : new BidfloorDTO(imp.getBidfloor(), null);
                    wrapper.setBidfloor(bidfloorDTO.getBidfloor());
                    wrapper.setDealid(bidfloorDTO.getDealid());
                    res.put(wrapper.getAdDTO().getAd().getId(), wrapper);
                }
            }
        } catch (Exception e) {
            // 超时,取消任务,如果整个任务或者部分任务还在缓存队列中，这里的取消是有用的
            total.cancel(false);
            for (CompletableFuture<AdDTOWrapper> future : futureList) {
                future.cancel(false);
            }
        }
        return res;
    }

    private AdDTOWrapper buildADDTOWrapper(String bidId, BidRequest bidRequest, String impId, AdDTO adDTO) {
        FrequencyCache frequencyCache = cacheService.getFrequencyCache();
        String deviceId = bidRequest.getDevice().getIfa();
        String campaignId = adDTO.getCampaign().getId().toString();

        AdDTOWrapper wrapper = new AdDTOWrapper(impId, bidId, adDTO);
        wrapper.setImpFrequency(frequencyCache.getImpCountToday(campaignId, deviceId));
        wrapper.setClickFrequency(frequencyCache.getClickCountToday(campaignId, deviceId));
        wrapper.setImpFrequencyHour(frequencyCache.getImpCountByHour(campaignId, deviceId));
        wrapper.setClickFrequencyHour(frequencyCache.getClickCountByHour(campaignId, deviceId));
        return wrapper;
    }

    private <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> com) {
        return CompletableFuture.allOf(com.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> com.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }
}
