package com.tecdo.fsm.task;

import com.dianping.cat.Cat;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.enums.AdvTypeEnum;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.fsm.task.handler.AdRecallHandler;
import com.tecdo.fsm.task.handler.PredictHandler;
import com.tecdo.fsm.task.handler.PriceCalcHandler;
import com.tecdo.fsm.task.handler.RtaHandler;
import com.tecdo.fsm.task.state.ITaskState;
import com.tecdo.fsm.task.state.InitState;
import com.tecdo.log.NotBidReasonLogger;
import com.tecdo.service.rta.Target;
import com.tecdo.transform.IProtoTransform;
import com.tecdo.util.ActionConsumeRecorder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.hutool.extra.spring.SpringUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class Task {

    private Imp imp;
    private BidRequest bidRequest;
    private Affiliate affiliate;
    private Long requestId;
    // taskId = bidId
    private String taskId;
    private IProtoTransform protoTransform;

    private int needReceiveCount = 0;
    private int predictResCount = 0;

    private int calcPriceResponseCount = 0;
    private final int calcPriceResponseNeed = 2;

    private int rtaResponseCount = 0;
    private final int rtaResponseNeed = 3;

    private final AdRecallHandler adRecallHandler = SpringUtil.getBean(AdRecallHandler.class);
    private final PredictHandler predictHandler = SpringUtil.getBean(PredictHandler.class);
    private final PriceCalcHandler priceCalcHandler = SpringUtil.getBean(PriceCalcHandler.class);
    private final RtaHandler rtaHandler = SpringUtil.getBean(RtaHandler.class);

    private final SoftTimer softTimer = SpringUtil.getBean(SoftTimer.class);
    private final MessageQueue messageQueue = SpringUtil.getBean(MessageQueue.class);
    private final Map<EventType, Long> eventTimerMap = new HashMap<>();
    private final Map<Integer, AdDTOWrapper> afterPredictAdMap = new HashMap<>();
    private Map<Integer, AdDTOWrapper> afterPriceFilterAdMap = new HashMap<>();
    private List<AdDTOWrapper> normalOrRtaTrueAds = Collections.emptyList();

    private ActionConsumeRecorder recorder = new ActionConsumeRecorder();

    private ITaskState currentState = SpringUtil.getBean(InitState.class);

    public void init(BidRequest bidRequest,
                     Imp imp,
                     Affiliate affiliate,
                     Long requestId,
                     String taskId,
                     IProtoTransform protoTransform) {
        this.bidRequest = bidRequest;
        this.imp = imp;
        this.affiliate = affiliate;
        this.requestId = requestId;
        this.taskId = taskId;
        this.protoTransform = protoTransform;
    }

    public void reset() {
        this.bidRequest = null;
        this.imp = null;
        this.affiliate = null;
        this.requestId = null;
        this.taskId = null;
        this.eventTimerMap.clear();
        this.currentState = SpringUtil.getBean(InitState.class);
        this.needReceiveCount = 0;
        this.predictResCount = 0;
        this.calcPriceResponseCount = 0;
        this.rtaResponseCount = 0;
        this.afterPredictAdMap.clear();
        this.afterPriceFilterAdMap.clear();
        this.normalOrRtaTrueAds.clear();
        this.protoTransform = null;
        this.recorder.reset();
    }

    public void switchState(ITaskState newState) {
        this.currentState = newState;
    }

    public void startTimer(EventType eventType, Params params, long delay) {
        long timerId = softTimer.startTimer(eventType, params, delay);
        eventTimerMap.put(eventType, timerId);
    }

    public void cancelTimer(EventType eventType) {
        if (eventTimerMap.containsKey(eventType)) {
            softTimer.cancel(eventTimerMap.get(eventType));
        } else {
            log.warn("taskId: {}, not exist this timer: {}", taskId, eventType);
        }
    }

    public void handleEvent(EventType eventType, Params params) {
        currentState.handleEvent(eventType, params, this);
    }

    public void tick(String action) {
        recorder.tick(action);
    }

    public void record() {
        recorder.stop();
        Map<String, Double> costMap = recorder.consumes();
        costMap.forEach((k, v) -> {
            Cat.logMetricForDuration(k, v.longValue());
        });
    }

    public Params assignParams() {
        return Params.create(ParamKey.REQUEST_ID, requestId).put(ParamKey.TASK_ID, taskId);
    }

    public void notifyFailed() {
        record();
        messageQueue.putMessage(EventType.BID_TASK_FAILED, assignParams());
    }

    public void adRecall(boolean recallBatchEnable) {
        adRecallHandler.adRecall(assignParams(),
                this.bidRequest, this.imp, this.affiliate, recallBatchEnable);
    }

    public void impNotBid() {
        Params params = assignParams().put(ParamKey.ADS_TASK_RESPONSE, Collections.emptyList());
        messageQueue.putMessage(EventType.BID_TASK_FINISH, params);
        record();
    }

    public void callPredictApi(Map<Integer, AdDTOWrapper> adDTOMap) {
        this.needReceiveCount = predictHandler.callPredictApi(adDTOMap, assignParams(),
                this.bidRequest, this.imp, this.affiliate, this.protoTransform);
    }

    public void savePredictResponse(Map<Integer, AdDTOWrapper> adDTOMap) {
        this.afterPredictAdMap.putAll(adDTOMap);
        this.predictResCount++;
    }

    public boolean isReceiveAllPredictResponse() {
        return predictResCount == needReceiveCount;
    }

    public void calcPrice() {
        priceCalcHandler.calcPrice(assignParams(), this.afterPredictAdMap,
                this.bidRequest, this.imp, this.affiliate);
    }

    public boolean calcPriceResponseFinish() {
        return ++calcPriceResponseCount == calcPriceResponseNeed;
    }

    public void priceFilter(Map<Integer, AdDTOWrapper> adDTOMap) {
        // 过滤掉出价低于底价的广告
        Map<Integer, AdDTOWrapper> map = new HashMap<>();
        for (AdDTOWrapper e : adDTOMap.values()) {
            if (e.getBidPrice()
                 .compareTo(BigDecimal.valueOf(Optional.of(imp.getBidfloor()).orElse(0f))) >= 0) {
                map.put(e.getAdDTO().getAd().getId(), e);
            } else {
                NotBidReasonLogger.log(taskId, e.getAdDTO().getAd().getId(), "bidFloorFilter");
            }
        }
        Params params = assignParams().put(ParamKey.ADS_PRICE_FILTER_RESPONSE, map);
        messageQueue.putMessage(EventType.PRICE_FILTER_FINISH, params);
        record();
    }

    public Map<Integer, AdDTOWrapper> savePriceFilterResponse(Params params) {
        this.afterPriceFilterAdMap = params.get(ParamKey.ADS_PRICE_FILTER_RESPONSE);
        log.info("receive ad from price filter, contextId: {}, taskId: {}, size:{}",
                requestId, taskId, this.afterPriceFilterAdMap.size());
        return this.afterPriceFilterAdMap;
    }

    public void requestRta() {
        rtaHandler.requestRta(assignParams(), afterPriceFilterAdMap, this.bidRequest);
    }

    public boolean rtaResponseFinish() {
        return ++rtaResponseCount == rtaResponseNeed;
    }

    public void saveRtaResponse(Params params) {
        Map<Integer, Target> lazadaRtaMap = params.get(ParamKey.REQUEST_LAZADA_RTA_RESPONSE);
        Map<Integer, Target> aeRtaMap = params.get(ParamKey.REQUEST_AE_RTA_RESPONSE);
        Map<Integer, Target> miraviaRtaMap = params.get(ParamKey.REQUEST_MIRAVIA_RTA_RESPONSE);
        Map<Integer, Target> mergeRtaMap = Stream.of(lazadaRtaMap, aeRtaMap, miraviaRtaMap)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue));

        Map<Integer, List<AdDTOWrapper>> campaignIdToAdList = //
                this.afterPriceFilterAdMap.values().stream()
                        .collect(Collectors.groupingBy(i -> i.getAdDTO().getCampaign().getId()));
        // 将rta匹配的结果保存到AdDTOWrapper中
        for (Map.Entry<Integer, Target> entry : mergeRtaMap.entrySet()) {
            Integer campaignId = entry.getKey();
            Target t = entry.getValue();
            campaignIdToAdList.get(campaignId).forEach(ad -> {
                ad.setRtaRequest(1);
                ad.setRtaRequestTrue(t.isTarget() ? 1 : 0);
                switch (AdvTypeEnum.of(t.getAdvType())) {
                    case LAZADA_RTA:
                    case MIRAVIA_RTA:
                        ad.setRtaToken(t.isTarget() ? t.getToken() : null);
                        break;
                    case AE_RTA:
                        ad.setLandingPage(t.getLandingPage());
                        ad.setDeeplink(t.getDeeplink());
                        break;
                }
            });
        }
        // 只保留非rta的单子 和 rta并且匹配的单子
        List<AdDTOWrapper> list = new ArrayList<>();
        for (AdDTOWrapper i : this.afterPriceFilterAdMap.values()) {
            if (i.getAdDTO().getCampaignRtaInfo() == null || i.getRtaRequestTrue() == 1) {
                list.add(i);
            } else {
                NotBidReasonLogger.log(taskId, i.getAdDTO().getAd().getId(), "rtaFilter");
            }
        }
        this.normalOrRtaTrueAds = list;
        log.info("contextId: {}, after rta filter, size: {}", requestId, normalOrRtaTrueAds.size());
    }

    public boolean isImpBid() {
        return this.normalOrRtaTrueAds.size() > 0;
    }

    public void sort() {
        normalOrRtaTrueAds.sort(Comparator.comparing(AdDTOWrapper::getBidPrice).reversed());
        messageQueue.putMessage(EventType.SORT_AD_RESPONSE,
                assignParams().put(ParamKey.SORT_AD_RESPONSE, normalOrRtaTrueAds));
    }

    public void saveSortAdResponse(Params params) {
        List<AdDTOWrapper> afterSortAds = params.get(ParamKey.SORT_AD_RESPONSE);
        log.info("contextId: {}, after sort response, size:{}", requestId, afterSortAds.size());
        params = assignParams().put(ParamKey.ADS_TASK_RESPONSE, afterSortAds);
        messageQueue.putMessage(EventType.BID_TASK_FINISH, params);
    }
}