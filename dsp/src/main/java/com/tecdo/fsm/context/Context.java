package com.tecdo.fsm.context;

import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.dianping.cat.Cat;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.doris.entity.GooglePlayApp;
import com.tecdo.common.constant.HttpCode;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.domain.openrtb.response.BidResponse;
import com.tecdo.fsm.context.state.IContextState;
import com.tecdo.fsm.context.state.InitState;
import com.tecdo.fsm.task.Task;
import com.tecdo.fsm.task.TaskPool;
import com.tecdo.log.RequestLogger;
import com.tecdo.log.ResponseLogger;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.service.init.doris.GooglePlayAppManager;
import com.tecdo.transform.IProtoTransform;
import com.tecdo.transform.ProtoTransformFactory;
import com.tecdo.util.ActionConsumeRecorder;
import com.tecdo.util.JsonHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class Context {

  private IContextState currentState = SpringUtil.getBean(InitState.class);

  private HttpRequest httpRequest;

  private BidRequest bidRequest;

  private Affiliate affiliate;

  private Long requestId;

  private ActionConsumeRecorder recorder = new ActionConsumeRecorder();

  private Map<String, Task> taskMap = new HashMap<>();
  // taskId,adId,AdDTOWrapper
  private Map<String, List<AdDTOWrapper>> taskResponse = new HashMap<>();
  private Map<String, AdDTOWrapper> impBidAdMap = new HashMap<>();

  private Map<EventType, Long> eventTimerMap = new HashMap<>();

  private IProtoTransform protoTransform;

  private final MessageQueue messageQueue = SpringUtil.getBean(MessageQueue.class);
  private final SoftTimer softTimer = SpringUtil.getBean(SoftTimer.class);

  private final TaskPool taskPool = SpringUtil.getBean(TaskPool.class);

  private final GooglePlayAppManager googlePlayAppManager = SpringUtil.getBean(GooglePlayAppManager.class);
  private final ResponseLogger responseLogger = SpringUtil.getBean(ResponseLogger.class);

  public void handleEvent(EventType eventType, Params params) {
    currentState.handleEvent(eventType, params, this);
  }

  public void init(HttpRequest httpRequest, BidRequest bidRequest, Affiliate affiliate) {
    this.httpRequest = httpRequest;
    this.bidRequest = bidRequest;
    this.affiliate = affiliate;
    this.requestId = httpRequest.getRequestId();
    this.protoTransform = ProtoTransformFactory.getProtoTransform(affiliate.getApi());
  }

  public void reset() {
    this.currentState = SpringUtil.getBean(InitState.class);
    this.bidRequest = null;
    this.httpRequest = null;
    this.affiliate = null;
    this.requestId = null;
    this.taskMap.values().forEach(taskPool::release);
    this.taskMap.clear();
    this.taskResponse.clear();
    this.impBidAdMap.clear();
    this.eventTimerMap.clear();
    this.protoTransform = null;
    this.recorder.reset();
  }

  public void switchState(IContextState newState) {
    this.currentState = newState;
  }

  public Params assignParams() {
    return Params.create(ParamKey.REQUEST_ID, requestId);
  }

  public void startTimer(EventType eventType, Params params, long delay) {
    long timerId = softTimer.startTimer(eventType, params, delay);
    eventTimerMap.put(eventType, timerId);
  }

  public void cancelTimer(EventType eventType) {
    if (eventTimerMap.containsKey(eventType)) {
      softTimer.cancel(eventTimerMap.get(eventType));
    } else {
      log.warn("contextId: {},not exist this timer：{}", requestId, eventType);
    }
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

  public void handleBidRequest() {
    String bidRequestString = JsonHelper.toJSONString(bidRequest);
    log.info("contextId: {}, affiliateId: {}, bid request is:{}", requestId, affiliate.getId(), bidRequestString);
    List<Imp> impList = bidRequest.getImp();
    impList.forEach(imp -> {
      Task task = taskPool.get();
      // taskId = bidId
      String taskId = generateBidId();
      task.init(bidRequest, imp, affiliate, requestId, taskId, protoTransform);
      taskMap.put(taskId, task);
      messageQueue.putMessage(EventType.TASK_START, assignParams().put(ParamKey.TASK_ID, taskId));
      log.info("receive bid request: {},requestId: {},taskId: {}",
               bidRequest.getId(),
               requestId,
               taskId);
    });
  }

  // 32位UUID + 13位时间戳
  private String generateBidId() {
    return IdUtil.fastSimpleUUID() + System.currentTimeMillis();
  }

  public void dispatchToTask(EventType eventType, Params params) {
    String taskId = params.get(ParamKey.TASK_ID);
    Task task = taskMap.get(taskId);
    if (task != null) {
      task.handleEvent(eventType, params);
    } else {
      log.error("contextId: {},can't find task for taskId: {}", requestId, taskId);
    }
  }

  public void saveTaskResponse(Params params) {
    String taskId = params.get(ParamKey.TASK_ID);
    List<AdDTOWrapper> afterSortAds = params.get(ParamKey.ADS_TASK_RESPONSE);
    taskResponse.put(taskId, afterSortAds);
    log.info("receive ad from task, contextId: {}, taskId: {}, afterSortAds size:{}, taskResponse size: {}",
             requestId, taskId, afterSortAds.size(), taskResponse.size());
  }

  public boolean isReceiveAllTaskResponse() {
    return taskResponse.size() == taskMap.size();
  }

  public void distinct() {
    Map<String, AdDTOWrapper> impBidAdMap = new HashMap<>();
    Set<Integer> hasBidAdId = new HashSet<>();
    for (Map.Entry<String, List<AdDTOWrapper>> entry : taskResponse.entrySet()) {
      String taskId = entry.getKey();
      List<AdDTOWrapper> afterSortAds = entry.getValue();
      for (AdDTOWrapper wrapper : afterSortAds) {
        Integer adId = wrapper.getAdDTO().getAd().getId();
        if (!impBidAdMap.containsKey(taskId) && !hasBidAdId.contains(adId)) {
          hasBidAdId.add(adId);
          impBidAdMap.put(taskId, wrapper);
        }
      }
    }
    messageQueue.putMessage(EventType.DISTINCT_AD_RESPONSE,
            assignParams().put(ParamKey. DISTINCT_AD_RESPONSE, impBidAdMap));
  }

  public void saveDistinctResponse(Params params) {
    this.impBidAdMap = params.get(ParamKey.DISTINCT_AD_RESPONSE);
    log.info("after distinct, contextId: {}, imp bid size: {}", requestId, impBidAdMap.size());
  }

  public void responseData() {
    Params params = Params.create();
    EventType eventType = EventType.RESPONSE_RESULT;
    logBidRequest();
    if (isBid()) {
      BidResponse bidResponse =
              protoTransform.responseTransform(this.impBidAdMap, this.bidRequest, this.affiliate);
      logBidResponse();
      String bidResponseString = JsonHelper.toJSONString(bidResponse);
      log.info("contextId: {}, bid response is:{}", requestId, bidResponseString);
      params.put(ParamKey.RESPONSE_BODY, bidResponseString);
      params.put(ParamKey.HTTP_CODE, HttpCode.OK);
    } else {
      params.put(ParamKey.HTTP_CODE, HttpCode.NOT_BID);
    }
    params.put(ParamKey.CHANNEL_CONTEXT, httpRequest.getChannelContext());
    messageQueue.putMessage(eventType, params);
    record();
  }

  public boolean isBid() {
    return this.impBidAdMap.size() > 0;
  }

  public void requestComplete() {
    messageQueue.putMessage(EventType.BID_REQUEST_COMPLETE, assignParams());
  }

  private void logBidRequest() {
    GooglePlayApp googleApp =
      googlePlayAppManager.getGoogleAppOrEmpty(bidRequest.getApp().getBundle());
    taskMap.forEach((taskId, task) -> {
      List<AdDTOWrapper> ads = taskResponse.getOrDefault(taskId, Collections.emptyList());
      int rtaRequest = ads.stream().anyMatch(i -> i.getRtaRequest() == 1) ? 1 : 0;
      int rtaRequestTrue = ads.stream().anyMatch(i -> i.getRtaRequestTrue() == 1) ? 1 : 0;
      RequestLogger.log(taskId,
                        task.getImp(),
                        bidRequest,
                        affiliate,
                        rtaRequest,
                        rtaRequestTrue,
                        googleApp);
    });
  }

  private void logBidResponse() {
    GooglePlayApp googleApp =
            googlePlayAppManager.getGoogleAppOrEmpty(bidRequest.getApp().getBundle());
    impBidAdMap.values().forEach(w -> responseLogger.log(w, bidRequest, affiliate, googleApp));
  }
}
