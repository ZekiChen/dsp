package com.tecdo.fsm.task.handler;

import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.filter.AbstractRecallFilter;
import com.tecdo.filter.factory.RecallFiltersFactory;
import com.tecdo.filter.util.FilterChainHelper;
import com.tecdo.service.init.AdManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 广告召回
 *
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

    @Value("${pac.timeout.task.ad.recall}")
    private int recallTimeout;

    public void adRecall(Params params, BidRequest bidRequest, Imp imp,
                         Affiliate affiliate, boolean recallBatchEnable) {
        threadPool.execute(() -> {
            try {
                Map<Integer, AdDTOWrapper> res;
                if (recallBatchEnable) {
                    res = doAdRecallBatch(params, bidRequest, imp, affiliate);
                } else {
                    res = doAdRecall(params, bidRequest, imp, affiliate);
                }
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
                .filter(adDTO -> FilterChainHelper.executeFilter(filters.get(0),
                        adDTO,
                        bidRequest,
                        imp,
                        affiliate))
                .collect(Collectors.toMap(adDTO -> adDTO.getAd().getId(),
                        adDTO -> new AdDTOWrapper(imp.getId(),
                                params.get(ParamKey.TASK_ID),
                                adDTO)));
    }

    private Map<Integer, AdDTOWrapper> doAdRecallBatch(Params params, BidRequest bidRequest,
                                                       Imp imp, Affiliate affiliate) {
        List<AbstractRecallFilter> filters = filtersFactory.createFilters();
        Map<Integer, AdDTO> adDTOMap = adManager.getAdDTOMap();
        List<CompletableFuture<AdDTOWrapper>> futureList = new ArrayList<>();
        Map<Integer, AdDTOWrapper> res = new HashMap<>();
        for (AdDTO adDTO : adDTOMap.values()) {
            // 在线程池中处理多个ad的recall
            CompletableFuture<AdDTOWrapper> future = CompletableFuture.supplyAsync(() -> {
                boolean match =
                        FilterChainHelper.executeFilter(filters.get(0), adDTO, bidRequest, imp, affiliate);
                if (match) {
                    return new AdDTOWrapper(imp.getId(), params.get(ParamKey.TASK_ID), adDTO);
                } else {
                    return null;
                }
            }, threadPool.getExecutor());
            futureList.add(future);
        }

        // 合并为一个future
        CompletableFuture<List<AdDTOWrapper>> total = sequence(futureList);
        try {
            List<AdDTOWrapper> adDTOWrappers = total.get(recallTimeout, TimeUnit.MILLISECONDS);
            for (AdDTOWrapper wrapper : adDTOWrappers) {
                if (wrapper != null) {
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

    private <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> com) {
        return CompletableFuture.allOf(com.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> com.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }
}
