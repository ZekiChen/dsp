package com.tecdo.fsm.task;

import com.tecdo.common.Params;
import com.tecdo.common.ThreadPool;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.Affiliate;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.AbstractRecallFilter;
import com.tecdo.filter.factory.RecallFiltersFactory;
import com.tecdo.filter.util.FilterChainUtil;
import com.tecdo.fsm.task.state.ITaskState;
import com.tecdo.fsm.task.state.InitState;
import com.tecdo.service.init.AdManager;
import com.tecdo.service.init.RtaInfoManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class Task {

  private Imp imp;
  private BidRequest bidRequest;
  private Affiliate affiliate;
  private long requestId;
  private String taskId;

  private final AdManager adManager = SpringUtil.getBean(AdManager.class);
  private final RtaInfoManager rtaInfoManager = SpringUtil.getBean(RtaInfoManager.class);
  private final RecallFiltersFactory filtersFactory =
    SpringUtil.getBean(RecallFiltersFactory.class);
  private final SoftTimer softTimer = SpringUtil.getBean(SoftTimer.class);
  private final MessageQueue messageQueue = SpringUtil.getBean(MessageQueue.class);
  private final Map<EventType, Long> eventTimerMap = new HashMap<>();

  private ITaskState currentState = SpringUtil.getBean(InitState.class);

  public void init(BidRequest bidRequest,
                   Imp imp,
                   Affiliate affiliate,
                   long requestId,
                   String taskId) {
    this.bidRequest = bidRequest;
    this.imp = imp;
    this.affiliate = affiliate;
    this.requestId = requestId;
    this.taskId = taskId;
  }

  public void reset() {

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
      log.warn("context: {}, not exist this timer: {}", bidRequest.getId(), eventType);
    }
  }

  public void handleEvent(EventType eventType, Params params) {
    currentState.handleEvent(eventType, params, this);
  }

  public void listRecallAd() {
    Params params = assignParams();
    ThreadPool.getInstance().execute(() -> {
      try {
        params.put(ParamKey.ADS_IMP_KEY, doListRecallAd());
        messageQueue.putMessage(EventType.ADS_RECALL_FINISH, params);
      } catch (Exception e) {
        log.error(
          "list recall ad error, task id: {}, so this request will not participate in bidding, reason: {}",
          taskId,
          e.getMessage());
        messageQueue.putMessage(EventType.ADS_RECALL_ERROR, params);
      }
    });
  }

  /**
   * 广告召回
   */
  private Map<Integer, AdDTOWrapper> doListRecallAd() {
    List<AbstractRecallFilter> filters = filtersFactory.createFilters();
    FilterChainUtil.assemble(filters);

    Map<Integer, AdDTOWrapper> resMap = new HashMap<>();
    for (AdDTO adDTO : adManager.getAdDTOMap().values()) {
      List<TargetCondition> conditions = listLegalCondition(adDTO.getConditions());
      // 原则上不应该修改adDTO，因为所有的请求都是直接用的manager的数据，但是conditions的过滤对所有请求都适用，在这里操作也没有实际影响
      adDTO.setConditions(conditions);
      // 该 AD 没有定投需求，过滤掉
      if (CollUtil.isEmpty(conditions)) {
        log.warn("ad: {} doesn't have condition, direct recall", adDTO.getAd().getId());
        continue;
      }
      // 有定投需求，校验：每个 AD 都需要被所有 filter 判断一遍
      if (executeFilter(filters.get(0), adDTO)) {
        resMap.put(adDTO.getAd().getId(), new AdDTOWrapper(adDTO));
      }
    }
    return resMap;
  }

  /**
   * 获取参数合法的condition对象（即非空校验）
   */
  private List<TargetCondition> listLegalCondition(List<TargetCondition> conditions) {
    return conditions.stream()
                     .filter(e -> e != null && StrUtil.isAllNotBlank(e.getAttribute(),
                                                                     e.getOperation(),
                                                                     e.getValue()))
                     .collect(Collectors.toList());
  }

  /**
   * 每个 AD 都需要被所有 filter 判断一遍
   */
  private boolean executeFilter(AbstractRecallFilter curFilter, AdDTO adDTO) {
    boolean filterFlag = curFilter.doFilter(bidRequest, imp, adDTO, affiliate);
    while (filterFlag && curFilter.hasNext()) {
      curFilter = curFilter.getNextFilter();
      filterFlag = curFilter.doFilter(bidRequest, imp, adDTO, affiliate);
    }
    return filterFlag;
  }

  public Params assignParams() {
    return Params.create(ParamKey.REQUEST_ID, requestId).put(ParamKey.TASK_ID, taskId);
  }
}
