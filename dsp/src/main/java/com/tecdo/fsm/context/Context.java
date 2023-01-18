package com.tecdo.fsm.context;

import com.tecdo.common.Params;
import com.tecdo.common.ThreadPool;
import com.tecdo.constant.EventType;
import com.tecdo.constant.HttpCode;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.domain.openrtb.response.Bid;
import com.tecdo.domain.openrtb.response.BidResponse;
import com.tecdo.domain.openrtb.response.SeatBid;
import com.tecdo.domain.openrtb.response.n.NativeResponse;
import com.tecdo.entity.Ad;
import com.tecdo.entity.Affiliate;
import com.tecdo.entity.RtaInfo;
import com.tecdo.enums.biz.AdTypeEnum;
import com.tecdo.fsm.task.Task;
import com.tecdo.fsm.task.TaskPool;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.service.init.RtaInfoManager;
import com.tecdo.service.rta.RtaHelper;
import com.tecdo.service.rta.Target;
import com.tecdo.util.AdmGenerator;
import com.tecdo.util.JsonHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Context {

  private IContextState currentState = SpringUtil.getBean(InitState.class);

  private HttpRequest httpRequest;

  private BidRequest bidRequest;

  private Affiliate affiliate;

  private AdDTO response;

  private Long requestId;

  private Map<String, Task> taskMap = new HashMap<>();

  private Map<String, Object> taskResponse = new HashMap<>();

  private Map<EventType, Long> eventTimerMap = new HashMap<>();

  private final MessageQueue messageQueue = SpringUtil.getBean(MessageQueue.class);

  private final SoftTimer softTimer = SpringUtil.getBean(SoftTimer.class);

  private final TaskPool taskPool = SpringUtil.getBean(TaskPool.class);

  private final RtaInfoManager rtaInfoManager = SpringUtil.getBean(RtaInfoManager.class);


  public void handleEvent(EventType eventType, Params params) {
    currentState.handleEvent(eventType, params, this);
  }

  public void init(HttpRequest httpRequest, BidRequest bidRequest, Affiliate affiliate) {
    this.httpRequest = httpRequest;
    this.bidRequest = bidRequest;
    this.affiliate = affiliate;
    this.requestId = httpRequest.getRequestId();
  }

  public void handleBidRequest() {
    List<Imp> impList = bidRequest.getImp();
    impList.forEach(imp -> {
      Task task = taskPool.get();
      task.init(bidRequest, imp);
      taskMap.put(imp.getId(), task);
      messageQueue.putMessage(EventType.TASK_START,
                              assignParams().put(ParamKey.TASK_ID, imp.getId()));
    });
  }

  public void dispatchToTask(EventType eventType, Params params) {
    String taskId = params.get(ParamKey.TASK_ID);
    Task task = taskMap.get(taskId);
    if (task != null) {
      task.handleEvent(eventType, params);
    } else {
      log.error("requestId:{},can't find task for taskId:{}", requestId, taskId);
    }
  }

  public void saveTaskResponse(Params params) {

  }

  public boolean isReceiveAllTaskResponse() {
    return taskResponse.size() == taskMap.size();
  }

  public void requestRta() {
    Params params = assignParams();
    ThreadPool.getInstance().execute(() -> {
      try {
        Map<Integer, Target> rtaResMap = doRequestRta();
        messageQueue.putMessage(EventType.REQUEST_RTA_RESPONSE,
                                params.put(ParamKey.REQUEST_RTA_RESPONSE, rtaResMap));
      } catch (Exception e) {
        messageQueue.putMessage(EventType.WAIT_REQUEST_RTA_RESPONSE_ERROR, params);
      }
    });
  }

  public Map<Integer, Target> doRequestRta() {
    List<AdDTO> adDTOList = null;
    // todo 协议中的是国家三字码，需要转为对应的二字码
    String country = bidRequest.getDevice().getGeo().getCountry();
    String deviceId = bidRequest.getDevice().getIfa();
    Map<Integer, Target> rtaResMap = new HashMap<>();

    // 只保留rta的单子，并将单子按照广告主分组
    Map<Integer, List<AdDTO>> advToAdList = adDTOList.stream()
                                                     .filter(i -> Objects.nonNull(i.getCampaignRtaInfo()))
                                                     .collect(Collectors.groupingBy(adDTO -> adDTO.getCampaignRtaInfo()
                                                                                                  .getAdvId()));
    // 分广告主进行rta匹配
    advToAdList.forEach((advId, adList) -> {
      RtaInfo rtaInfo = rtaInfoManager.getRtaInfo(advId);
      RtaHelper.requestRta(rtaInfo, adList, country, deviceId, rtaResMap);
    });
    return rtaResMap;
  }

  public void saveRtaResponse(Params params) {
    List<AdDTO> adDTOList = null;
    Map<Integer, Target> rtaResMap = params.get(ParamKey.REQUEST_RTA_RESPONSE);
    Map<Integer, List<AdDTO>> campaignIdToAdList =
      adDTOList.stream().collect(Collectors.groupingBy(adDTO -> adDTO.getCampaign().getId()));
    // 将rta匹配的结果保存到AdDTO中
    for (Map.Entry<Integer, Target> entry : rtaResMap.entrySet()) {
      Integer campaignId = entry.getKey();
      Target t = entry.getValue();
      if (t.isTarget()) {
        String token = t.getToken();
        campaignIdToAdList.get(campaignId).forEach(i -> i.setRtaToken(token));
      }
    }
    // 只保留非rta的单子 和 rta并别匹配的单子
    adDTOList = adDTOList.stream()
                         .filter(i -> i.getCampaignRtaInfo() == null || i.getRtaToken() != null)
                         .collect(Collectors.toList());
  }

  public void sort() {
    List<AdDTO> adDTOList = null;
    AdDTO res = null;
    Double calc = Double.MIN_VALUE;
    for (AdDTO adDTO : adDTOList) {
      double temp = adDTO.getBidPrice() * adDTO.getPCtr();
      if (calc < temp) {
        calc = temp;
        res = adDTO;
      }
    }
    messageQueue.putMessage(EventType.SORT_AD_RESPONSE,
                            assignParams().put(ParamKey.SORT_AD_RESPONSE, res));
  }

  public void saveSortAdResponse(Params params) {
    AdDTO adDTO = params.get(ParamKey.SORT_AD_RESPONSE);
    this.response = adDTO;
  }

  public void responseData() {
    Params params = Params.create();
    EventType eventType = EventType.RESPONSE_RESULT;
    if (this.response == null) {
      params.put(ParamKey.HTTP_CODE, HttpCode.NOT_BID);
      params.put(ParamKey.CHANNEL_CONTEXT, httpRequest.getChannelContext());
    } else {
      BidResponse bidResponse = buildResponse(this.response);
      params.put(ParamKey.RESPONSE_BODY, JsonHelper.toJSONString(bidResponse));
      params.put(ParamKey.HTTP_CODE, HttpCode.OK);
      params.put(ParamKey.CHANNEL_CONTEXT, httpRequest.getChannelContext());
    }
    messageQueue.putMessage(eventType, params);
  }

  private BidResponse buildResponse(AdDTO adDTO) {
    String bidId = generateBidId();
    BidResponse bidResponse = new BidResponse();
    bidResponse.setId(bidRequest.getId());
    bidResponse.setBidid(bidId);
    Bid bid = new Bid();
    bid.setId(bidId);
    bid.setImpid(adDTO.getImpId());
    bid.setPrice(adDTO.getBidPrice().floatValue());
    // todo 曝光链接等
    bid.setNurl("");
    bid.setBurl("");
    bid.setAdm(buildAdm(adDTO));
    bid.setAdid(String.valueOf(adDTO.getAd().getId()));
    bid.setAdomain(Collections.singletonList(adDTO.getCampaign().getDomain()));
    bid.setBundle(adDTO.getCampaign().getPackageName());
    bid.setIurl(adDTO.getCreativeMap().get(getCreativeIdByAd(adDTO.getAd())).getUrl());
    bid.setCid(String.valueOf(adDTO.getCampaign().getId()));
    bid.setCrid(String.valueOf(getCreativeIdByAd(adDTO.getAd())));

    String adm = buildAdm(adDTO);

    bid.setAdm(adm);

    SeatBid seatBid = new SeatBid();
    seatBid.setBid(Collections.singletonList(bid));
    bidResponse.setSeatbid(Collections.singletonList(seatBid));

    return bidResponse;
  }

  private Integer getCreativeIdByAd(Ad ad) {
    switch (AdTypeEnum.of(ad.getType())) {
      case BANNER:
      case NATIVE:
        return ad.getImage();
      case VIDEO:
        return ad.getVideo();
    }
    return null;
  }

  private String buildAdm(AdDTO adDTO) {
    String adm = null;
    if (Objects.equals(adDTO.getAd().getType(), AdTypeEnum.BANNER.getType())) {
      // todo clickurl deeplink 都需要做format
      adm = AdmGenerator.bannerAdm(adDTO.getAdGroup().getClickUrl(),
                                   adDTO.getAdGroup().getDeeplink(),
                                   adDTO.getCreativeMap().get(adDTO.getAd().getImage()).getUrl(),
                                   Arrays.asList(adDTO.getAdGroup().getImpTrackUrls().split(",")),
                                   Arrays.asList(adDTO.getAdGroup()
                                                      .getClickTrackUrls()
                                                      .split(",")));
    }
    if (Objects.equals(adDTO.getAd().getType(), AdTypeEnum.NATIVE.getType())) {
      List<Imp> impList = bidRequest.getImp();
      Imp imp =
        impList.stream().filter(i -> Objects.equals(i.getId(), adDTO.getImpId())).findFirst().get();
      NativeResponse nativeResponse = //
        AdmGenerator.nativeAdm(imp.getNative1().getNativeRequest(),
                               adDTO,
                               adDTO.getAdGroup().getClickUrl(),
                               adDTO.getAdGroup().getDeeplink(),
                               Arrays.asList(adDTO.getAdGroup().getImpTrackUrls().split(",")),
                               Arrays.asList(adDTO.getAdGroup().getClickTrackUrls().split(",")));
      adm = JsonHelper.toJSONString(nativeResponse);
    }
    return adm;
  }

  private String generateBidId() {
    return UUID.randomUUID().toString() + System.currentTimeMillis();
  }

  public void requestComplete() {
    messageQueue.putMessage(EventType.BID_REQUEST_COMPLETE, assignParams());
  }

  public void reset() {
    this.currentState = SpringUtil.getBean(InitState.class);
    this.bidRequest = null;
    this.httpRequest = null;
    this.affiliate = null;
    this.requestId = null;
    this.taskMap.clear();
    this.taskResponse.clear();
    this.eventTimerMap.clear();
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
      log.warn("context:{},not exist this timer：{}", bidRequest.getId(), eventType);
    }

  }

}
