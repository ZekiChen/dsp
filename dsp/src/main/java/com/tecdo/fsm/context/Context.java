package com.tecdo.fsm.context;

import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.RtaInfo;
import com.tecdo.adm.api.delivery.enums.AdvEnum;
import com.tecdo.common.constant.HttpCode;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.biz.notice.NoticeInfo;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.domain.openrtb.response.BidResponse;
import com.tecdo.entity.doris.GooglePlayApp;
import com.tecdo.fsm.task.Task;
import com.tecdo.fsm.task.TaskPool;
import com.tecdo.log.RequestLogger;
import com.tecdo.log.ResponseLogger;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.service.CacheService;
import com.tecdo.service.init.AdvManager;
import com.tecdo.service.init.GooglePlayAppManager;
import com.tecdo.service.init.RtaInfoManager;
import com.tecdo.service.rta.RtaHelper;
import com.tecdo.service.rta.Target;
import com.tecdo.service.rta.ae.AeRtaInfoVO;
import com.tecdo.transform.IProtoTransform;
import com.tecdo.transform.ProtoTransformFactory;
import com.tecdo.util.CreativeHelper;
import com.tecdo.util.JsonHelper;
import com.tecdo.util.StringConfigUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class Context {

  private IContextState currentState = SpringUtil.getBean(InitState.class);

  private HttpRequest httpRequest;

  private BidRequest bidRequest;

  private Affiliate affiliate;

  private AdDTOWrapper response = null;

  private Long requestId;

  private Map<String, Task> taskMap = new HashMap<>();
  // taskId,adId,AdDTOWrapper
  private Map<String, Map<Integer, AdDTOWrapper>> taskResponse = new HashMap<>();

  private List<AdDTOWrapper> adDTOWrapperList = new ArrayList<>();

  private Map<EventType, Long> eventTimerMap = new HashMap<>();

  private IProtoTransform protoTransform;

  private final MessageQueue messageQueue = SpringUtil.getBean(MessageQueue.class);

  private final SoftTimer softTimer = SpringUtil.getBean(SoftTimer.class);

  private final ThreadPool threadPool = SpringUtil.getBean(ThreadPool.class);

  private final TaskPool taskPool = SpringUtil.getBean(TaskPool.class);

  private final RtaInfoManager rtaInfoManager = SpringUtil.getBean(RtaInfoManager.class);

  private int  rtaResponseCount = 0;
  private final int rtaResponseNeed = 2;

  private final GooglePlayAppManager googlePlayAppManager =
    SpringUtil.getBean(GooglePlayAppManager.class);

  private final AdvManager advManager = SpringUtil.getBean(AdvManager.class);

  private final CacheService cacheService = SpringUtil.getBean(CacheService.class);

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
    this.response = null;
    this.requestId = null;
    this.taskMap.values().forEach(taskPool::release);
    this.taskMap.clear();
    this.taskResponse.clear();
    this.adDTOWrapperList.clear();
    this.eventTimerMap.clear();
    this.protoTransform = null;
    this.rtaResponseCount = 0;
  }

  public void handleBidRequest() {
    String bidRequestString = JsonHelper.toJSONString(bidRequest);
//    log.info("contextId: {}, bid request is:{}", requestId, bidRequestString);
    log.info("contextId: {}, bid request", requestId);
    List<Imp> impList = bidRequest.getImp();
    impList.forEach(imp -> {
      Task task = taskPool.get();
      // taskId = bidId
      String taskId = generateBidId();
      task.init(bidRequest, imp, affiliate, requestId, taskId);
      taskMap.put(taskId, task);
      messageQueue.putMessage(EventType.TASK_START, assignParams().put(ParamKey.TASK_ID, taskId));
      log.info("receive bid request: {},requestId: {},taskId: {}",
               bidRequest.getId(),
               requestId,
               taskId);
    });
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
    Map<Integer, AdDTOWrapper> adDTOWrapperMap = params.get(ParamKey.ADS_TASK_RESPONSE);
    taskResponse.put(taskId, adDTOWrapperMap);
    log.info("receive ad from task,contextId: {},taskId: {},size:{}",
             requestId,
             taskId,
             adDTOWrapperMap.size());
  }

  public boolean isReceiveAllTaskResponse() {
    return taskResponse.size() == taskMap.size();
  }

  public boolean checkTaskResponse() {
    this.adDTOWrapperList = taskResponse.values()
                                        .stream()
                                        .flatMap(value -> value.values().stream())
                                        .collect(Collectors.toList());
    return adDTOWrapperList.size() > 0;
  }

  public boolean checkResponse() {
    return adDTOWrapperList.size() > 0;
  }

  public boolean rtaResponseFinish() {
    return ++rtaResponseCount == rtaResponseNeed;
  }

  public void requestRta() {
    Params params = assignParams();
    BidRequest bidRequest = this.bidRequest;

    threadPool.execute(() -> {
      try {
        Map<Integer, Target> rtaResMap = doRequestRtaByLazada(bidRequest);
        messageQueue.putMessage(EventType.REQUEST_RTA_RESPONSE,
                params.put(ParamKey.REQUEST_LAZADA_RTA_RESPONSE, rtaResMap));
      } catch (Exception e) {
        log.error("contextId: {}, request lazada rta cause a exception:", requestId, e);
        messageQueue.putMessage(EventType.WAIT_REQUEST_RTA_RESPONSE_ERROR, params);
      }
    });

    threadPool.execute(() -> {
      try {
        Map<Integer, Target> rtaResMap = doRequestRtaByAE(bidRequest);
        messageQueue.putMessage(EventType.REQUEST_RTA_RESPONSE,
                params.put(ParamKey.REQUEST_AE_RTA_RESPONSE, rtaResMap));
      } catch (Exception e) {
        log.error("contextId: {}, request ae rta cause a exception:", requestId, e);
        messageQueue.putMessage(EventType.WAIT_REQUEST_RTA_RESPONSE_ERROR, params);
      }
    });

    log.info("contextId: {}, request rta", requestId);
  }

  private Map<Integer, Target> doRequestRtaByLazada(BidRequest bidRequest) {
    // 协议中的是国家三字码，需要转为对应的二字码
    String country = bidRequest.getDevice().getGeo().getCountry();
    String countryCode = StringConfigUtil.getCountryCode(country);
    String deviceId = bidRequest.getDevice().getIfa();
    Map<Integer, Target> rtaResMap = new HashMap<>();

    // 只保留lazada rta的单子，并将单子按照广告主分组
    Map<Integer, List<AdDTOWrapper>> advToAdList = //
      this.adDTOWrapperList.stream()
           .filter(i -> Objects.nonNull(i.getAdDTO().getCampaignRtaInfo())
                   && AdvEnum.LAZADA.getDesc().equals(advManager.getAdvName(i.getAdDTO().getCampaign().getAdvId())))
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

  private Map<Integer, Target> doRequestRtaByAE(BidRequest bidRequest) {
    String deviceId = bidRequest.getDevice().getIfa();
    // 只保留ae rta的单子
    Map<Integer, String> cid2AdvCid = this.adDTOWrapperList.stream()
            .filter(i -> Objects.nonNull(i.getAdDTO().getCampaignRtaInfo())
                    & AdvEnum.AE.getDesc().equals(advManager.getAdvName(i.getAdDTO().getCampaign().getAdvId())))
            .collect(Collectors.toMap(
                    ad -> ad.getAdDTO().getCampaign().getId(),
                    ad -> ad.getAdDTO().getCampaignRtaInfo().getAdvCampaignId(),
                    (o, n) -> o));
    Set<String> advCampaignIds = new HashSet<>(cid2AdvCid.values());
    List<AeRtaInfoVO> aeRtaInfoVOs = cacheService.getRtaCache().getAeRtaResponse(advCampaignIds, deviceId);
    Map<String, AeRtaInfoVO> advCId2AeRtaVOMap = aeRtaInfoVOs.stream()
            .collect(Collectors.toMap(AeRtaInfoVO::getAdvCampaignId, e -> e));

    return cid2AdvCid.entrySet().stream().map(entry -> {
      Integer campaignId = entry.getKey();
      String advCampaignId = entry.getValue();
      AeRtaInfoVO vo = advCId2AeRtaVOMap.get(advCampaignId);
      Target target = new Target();
      target.setAdvName(AdvEnum.AE.getDesc());
      target.setTarget(vo.getTarget());
      target.setLandingPage(vo.getLandingPage());  // cache sink 已经处理过了，取该层即可
      return new AbstractMap.SimpleEntry<>(campaignId, target);
    }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public void saveRtaResponse(Params params) {
    Map<Integer, Target> lazadaRtaMap = params.get(ParamKey.REQUEST_LAZADA_RTA_RESPONSE);
    Map<Integer, Target> aeRtaMap = params.get(ParamKey.REQUEST_AE_RTA_RESPONSE);
    Map<Integer, Target> mergeRtaMap = Stream.of(lazadaRtaMap, aeRtaMap)
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    Map<Integer, List<AdDTOWrapper>> campaignIdToAdList = //
            this.adDTOWrapperList.stream()
                    .collect(Collectors.groupingBy(i -> i.getAdDTO().getCampaign().getId()));
    // 将rta匹配的结果保存到AdDTOWrapper中
    for (Map.Entry<Integer, Target> entry : mergeRtaMap.entrySet()) {
      Integer campaignId = entry.getKey();
      Target t = entry.getValue();
      campaignIdToAdList.get(campaignId).forEach(ad -> {
        switch (AdvEnum.of(t.getAdvName())) {
          case LAZADA:
            ad.setRtaToken(t.isTarget() ? t.getToken() : null);
            ad.setLazadaRtaRequest(1);
            ad.setLazadaRtaRequestTrue(t.isTarget() ? 1 : 0);
            break;
          case AE:
            ad.setAeRtaRequest(1);
            ad.setAeRtaRequestTrue(t.isTarget() ? 1 : 0);
            ad.setLandingPage(t.getLandingPage());
            break;
          }
      });
    }
    // 只保留非rta的单子 和 rta并且匹配的单子
    this.adDTOWrapperList = this.adDTOWrapperList.stream()
            .filter(i -> i.getAdDTO().getCampaignRtaInfo() == null
                        || i.getLazadaRtaRequestTrue() == 1 || i.getAeRtaRequestTrue() == 1)
            .collect(Collectors.toList());
    log.info("contextId: {}, after rta filter, size: {}", requestId, adDTOWrapperList.size());
  }

  public void sort() {
    AdDTOWrapper res = null;
    double calc = Double.MIN_VALUE;
    for (AdDTOWrapper adDTOWrapper : adDTOWrapperList) {
      double temp = adDTOWrapper.getBidPrice().doubleValue();
      if (calc < temp) {
        calc = temp;
        res = adDTOWrapper;
      }
    }
    messageQueue.putMessage(EventType.SORT_AD_RESPONSE,
                            assignParams().put(ParamKey.SORT_AD_RESPONSE, res));
  }

  public void saveSortAdResponse(Params params) {
    AdDTOWrapper adDTOWrapper = params.get(ParamKey.SORT_AD_RESPONSE);
    this.response = adDTOWrapper;
    log.info("contextId: {},after sort response,adId:{}",
             requestId,
             response.getAdDTO().getAd().getId());
  }

  public void responseData() {
    Params params = Params.create();
    EventType eventType = EventType.RESPONSE_RESULT;
    logBidRequest();
    if (this.response == null) {
      params.put(ParamKey.HTTP_CODE, HttpCode.NOT_BID);
      params.put(ParamKey.CHANNEL_CONTEXT, httpRequest.getChannelContext());
    } else {
      cacheNoticeInfoByAe(response, bidRequest);
      logBidResponse();
      BidResponse bidResponse =
        protoTransform.responseTransform(this.response, this.bidRequest, this.affiliate);
      String bidResponseString = JsonHelper.toJSONString(bidResponse);
//      log.info("contextId: {}, bid response is:{}", requestId, bidResponseString);
      log.info("contextId: {}, bid response", requestId);
      params.put(ParamKey.RESPONSE_BODY, bidResponseString);
      params.put(ParamKey.HTTP_CODE, HttpCode.OK);
      params.put(ParamKey.CHANNEL_CONTEXT, httpRequest.getChannelContext());
    }
    messageQueue.putMessage(eventType, params);
  }

  // 32位UUID + 13位时间戳
  private String generateBidId() {
    return IdUtil.fastSimpleUUID() + System.currentTimeMillis();
  }

  public void requestComplete() {
    messageQueue.putMessage(EventType.BID_REQUEST_COMPLETE, assignParams());
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

  private void logBidResponse() {
    GooglePlayApp googleApp =
      googlePlayAppManager.getGoogleAppOrEmpty(bidRequest.getApp().getBundle());
    ResponseLogger.log(response, bidRequest, affiliate, googleApp);
  }

  private void logBidRequest() {
    GooglePlayApp googleApp =
      googlePlayAppManager.getGoogleAppOrEmpty(bidRequest.getApp().getBundle());
    taskMap.forEach((taskId, task) -> {
      Map<Integer, AdDTOWrapper> adDTOWrapperMap =
        taskResponse.getOrDefault(taskId, Collections.emptyMap());
      int lazadaRtaRequest =
        adDTOWrapperMap.values().stream().anyMatch(i -> i.getLazadaRtaRequest() == 1) ? 1 : 0;
      int lazadaRtaRequestTrue =
        adDTOWrapperMap.values().stream().anyMatch(i -> i.getLazadaRtaRequestTrue() == 1) ? 1 : 0;
      int aeRtaRequest =
        adDTOWrapperMap.values().stream().anyMatch(i -> i.getAeRtaRequest() == 1) ? 1 : 0;
      int aeRtaRequestTrue =
        adDTOWrapperMap.values().stream().anyMatch(i -> i.getAeRtaRequestTrue() == 1) ? 1 : 0;
      RequestLogger.log(taskId,
                        task.getImp(),
                        bidRequest,
                        affiliate,
                        lazadaRtaRequest,
                        lazadaRtaRequestTrue,
                        aeRtaRequest,
                        aeRtaRequestTrue,
                        googleApp);
    });
  }

  private void cacheNoticeInfoByAe(AdDTOWrapper adDTOWrapper, BidRequest bidRequest) {
    String advName = advManager.getAdvName(adDTOWrapper.getAdDTO().getCampaign().getAdvId());
    if (AdvEnum.AE.getDesc().equals(advName)) {
      Ad ad = adDTOWrapper.getAdDTO().getAd();
      NoticeInfo info = new NoticeInfo();
      info.setCampaignId(adDTOWrapper.getAdDTO().getCampaign().getId());
      info.setAdGroupId(adDTOWrapper.getAdDTO().getAdGroup().getId());
      info.setAdId(ad.getId());
      info.setCreativeId(CreativeHelper.getCreativeId(ad));
      info.setDeviceId(bidRequest.getDevice().getIfa());
      cacheService.getNoticeCache().setNoticeInfo(adDTOWrapper.getBidId(), info);
    }
  }
}
