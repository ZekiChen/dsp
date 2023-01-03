package com.tecdo.fsm.context;

import com.tecdo.common.Params;
import com.tecdo.common.ThreadPool;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.domain.dto.AdDTO;
import com.tecdo.domain.request.BidRequest;
import com.tecdo.domain.request.Imp;
import com.tecdo.entity.RtaInfo;
import com.tecdo.fsm.task.Task;
import com.tecdo.fsm.task.TaskPool;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.service.init.RtaInfoManager;
import com.tecdo.service.rta.RtaHelper;
import com.tecdo.service.rta.Target;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Context {

  private IContextState currentState;

  private HttpRequest httpRequest;

  private BidRequest bidRequest;

  private Map<String, Task> taskMap = new HashMap<>();

  private Map<String, Object> taskResponse = new HashMap<>();

  private Map<EventType, Long> eventTimerMap = new HashMap<>();

  private final MessageQueue messageQueue = SpringUtil.getBean(MessageQueue.class);

  private final SoftTimer softTimer = SpringUtil.getBean(SoftTimer.class);

  private final RtaInfoManager rtaInfoManager = SpringUtil.getBean(RtaInfoManager.class);


  public void handleEvent(EventType eventType, Params params) {
    currentState.handleEvent(eventType, params, this);
  }

  public void init(HttpRequest httpRequest, BidRequest bidRequest) {
    this.httpRequest = httpRequest;
    this.bidRequest = bidRequest;
  }

  public void handleBidRequest() {
    List<Imp> impList = bidRequest.getImp();
    impList.forEach(imp -> {
      Task task = TaskPool.getInstance().get();
      taskMap.put(imp.getId(), task);
      task.handleEvent(EventType.RECEIVE_BID_REQUEST,
                       Params.create(ParamKey.IMP, imp).put(ParamKey.BID_REQUEST, bidRequest));
    });
  }

  public void saveTaskResponse(Params params) {

  }

  public boolean isReceiveAllTaskResponse() {
    return taskResponse.size() == taskMap.size();
  }

  public void requestRta() {
    ThreadPool.getInstance().execute(() -> {

      try {
        Map<Integer, Target> rtaResMap = doRequestRta();
        messageQueue.putMessage(EventType.REQUEST_RTA_RESPONSE,
                                assignParams().put(ParamKey.REQUEST_RTA_RESPONSE, rtaResMap));
      } catch (Exception e) {

      }
    });
  }

  public Map<Integer, Target> doRequestRta() {
    List<AdDTO> adDTOList = null;
    // todo 协议中的是国家三字码，需要转为对应的二字码
    String country = bidRequest.getDevice().getGeo().getCountry();
    String deviceId = bidRequest.getDevice().getIfa();
    Map<Integer, Target> rtaResMap = new HashMap<>();

    Map<Integer, List<AdDTO>> advToAdList = adDTOList.stream()
                                                     .filter(i -> Objects.nonNull(i.getCampaignRtaInfo()))
                                                     .collect(Collectors.groupingBy(adDTO -> adDTO.getCampaignRtaInfo()
                                                                                                  .getAdvId()));
    advToAdList.forEach((advId, adList) -> {
      RtaInfo rtaInfo = rtaInfoManager.getRtaInfo(advId);
      RtaHelper.requestRta(rtaInfo, adList, country, deviceId, rtaResMap);
    });
    Map<Integer, List<AdDTO>> campaignIdToAdList =
      adDTOList.stream().collect(Collectors.groupingBy(adDTO -> adDTO.getCampaign().getId()));
    for (Map.Entry<Integer, Target> entry : rtaResMap.entrySet()) {
      Integer campaignId = entry.getKey();
      Target t = entry.getValue();
      if (t.isTarget()) {
        String token = t.getToken();
        campaignIdToAdList.get(campaignId).forEach(i -> i.setRtaToken(token));
      }
    }
    return rtaResMap;
  }

  public void saveRtaResponse(Params params) {

  }

  public void sort() {

  }

  public void saveSortAdResponse(Params params) {

  }

  public void responseData() {

  }

  public void buildResponse() {

  }

  private void buildAdm() {

  }

  public void requestComplete() {
    messageQueue.putMessage(EventType.BID_REQUEST_COMPLETE, assignParams());
  }

  public void reset() {

  }

  public void switchState(IContextState newState) {
    this.currentState = newState;
  }

  public Params assignParams() {
    return Params.create(ParamKey.REQUEST_ID, httpRequest.getRequestId());
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
