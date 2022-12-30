package com.tecdo.fsm.context;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.SoftTimer;
import com.tecdo.domain.dto.AdDTO;
import com.tecdo.domain.request.BidRequest;
import com.tecdo.domain.request.Imp;
import com.tecdo.entity.RtaInfo;
import com.tecdo.fsm.task.Task;
import com.tecdo.fsm.task.TaskPool;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.service.init.RtaInfoManager;
import com.tecdo.service.rta.RtaService;
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

  private Map<EventType, Long> eventTimerMap = new HashMap<>();


  private final SoftTimer softTimer = SpringUtil.getBean(SoftTimer.class);

  private final RtaInfoManager rtaInfoManager = SpringUtil.getBean(RtaInfoManager.class);

  private final RtaService rtaService = SpringUtil.getBean(RtaService.class);

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
      task.handleEvent(EventType.RECEIVE_BID_REQUEST,
                       Params.create(ParamKey.IMP, imp).put(ParamKey.BID_REQUEST, bidRequest));
      taskMap.put(imp.getId(), task);
    });
  }

  public void requestRta() {
    List<AdDTO> adDTOList = null;
    String country = bidRequest.getDevice().getGeo().getCountry();
    String deviceId = bidRequest.getDevice().getIfa();
    Map<Integer, Target> rtaResMap = new HashMap<>();

    Map<Integer, List<AdDTO>> advToAdList = adDTOList.stream()
                                                     .filter(i -> Objects.nonNull(i.getCampaignRtaInfo()))
                                                     .collect(Collectors.groupingBy(adDTO -> adDTO.getCampaignRtaInfo()
                                                                                                  .getAdvId()));
    advToAdList.forEach((advId, adList) -> {
      RtaInfo rtaInfo = rtaInfoManager.getRtaInfo(advId);
      rtaService.requestRta(rtaInfo, adList, country, deviceId, rtaResMap);
    });
    for (Map.Entry<Integer, Target> entry : rtaResMap.entrySet()) {
      Integer campaignId = entry.getKey();
      Target t = entry.getValue();
      if (t.isTarget()) {
        String token = t.getToken();
      }
    }
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

  private void cancelTimer(EventType eventType) {
    if (eventTimerMap.containsKey(eventType)) {
      softTimer.cancel(eventTimerMap.get(eventType));
    } else {
      log.warn("context:{},not exist this timer：{}", bidRequest.getId(), eventType);
    }

  }

}
