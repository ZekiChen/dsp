package com.tecdo.fsm.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.zhxu.okhttps.HttpResult;
import cn.zhxu.okhttps.OkHttps;
import com.tecdo.common.Params;
import com.tecdo.common.ThreadPool;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.domain.biz.base.R;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.biz.request.CtrRequest;
import com.tecdo.domain.biz.response.CtrResponse;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Geo;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.Affiliate;
import com.tecdo.entity.CampaignRtaInfo;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.AbstractRecallFilter;
import com.tecdo.filter.factory.RecallFiltersFactory;
import com.tecdo.filter.util.FilterChainHelper;
import com.tecdo.fsm.task.state.ITaskState;
import com.tecdo.fsm.task.state.InitState;
import com.tecdo.service.init.AdManager;
import com.tecdo.service.init.RtaInfoManager;
import com.tecdo.util.CreativeHelper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class Task {

  private Imp imp;
  private BidRequest bidRequest;
  private Affiliate affiliate;
  private Long requestId;
  private String taskId;

  private String ctrPredictUrl = SpringUtil.getProperty("pac.ctr-predict.url");

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
                   Long requestId,
                   String taskId) {
    this.bidRequest = bidRequest;
    this.imp = imp;
    this.affiliate = affiliate;
    this.requestId = requestId;
    this.taskId = taskId;
  }

  public void reset() {
    this.bidRequest = null;
    this.imp = null;
    this.affiliate = null;
    this.requestId = null;
    this.taskId = null;
    eventTimerMap.clear();
    currentState = SpringUtil.getBean(InitState.class);
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
        params.put(ParamKey.ADS_RECALL_RESPONSE, doListRecallAd());
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
    FilterChainHelper.assemble(filters);

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
        resMap.put(adDTO.getAd().getId(), new AdDTOWrapper(imp.getId(), adDTO));
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

  public void callCtr3Api(Map<Integer, AdDTOWrapper> adDTOMap) {
    Params params = assignParams();
    ThreadPool.getInstance().execute(() -> {
      HttpResult httpResult = buildAndCallCtr3Api(adDTOMap, affiliate.getId());
      if (httpResult.isSuccessful()) {
        R<List<CtrResponse>> result = httpResult.getBody().toBean(R.class);
        result.getData().forEach(resp -> {
          AdDTOWrapper wrapper = adDTOMap.get(resp.getAdId());
          wrapper.setPCtr(resp.getPCtr());
          wrapper.setPCtrVersion(result.getVersion());
        });
        params.put(ParamKey.ADS_P_CTR_RESPONSE, adDTOMap);
        messageQueue.putMessage(EventType.CTR_PREDICT_FINISH, params);
      } else {
        log.error("ctr request error: {}, reason: {}",
                  httpResult.getStatus(),
                  httpResult.getError().getMessage());
        messageQueue.putMessage(EventType.CTR_PREDICT_ERROR, params);
      }
    });
  }

  private HttpResult buildAndCallCtr3Api(Map<Integer, AdDTOWrapper> adDTOMap, Integer affId) {
    List<CtrRequest> ctrRequests = adDTOMap.values()
                                           .stream()
                                           .map(adDTOWrapper -> buildCtrRequest(bidRequest,
                                                                                imp,
                                                                                affId,
                                                                                adDTOWrapper.getAdDTO()))
                                           .collect(Collectors.toList());
    Map<String, Object> paramMap =
      MapUtil.<String, Object>builder().put("data", ctrRequests).build();
    return OkHttps.sync(ctrPredictUrl).addBodyPara(paramMap).get();
  }

  private CtrRequest buildCtrRequest(BidRequest bidRequest, Imp imp, Integer affId, AdDTO adDTO) {
    Integer creativeId = CreativeHelper.getCreativeId(adDTO.getAd());
    return CtrRequest.builder()
                     .adId(adDTO.getAd().getId())
                     .day(DateUtil.today())
                     .affiliateId(affId)
                     .adType(adDTO.getAd().getType().toString())
                     .adHeight(adDTO.getCreativeMap().get(creativeId).getHeight())
                     .adWidth(adDTO.getCreativeMap().get(creativeId).getWidth())
                     .os(bidRequest.getDevice().getOs())
                     .deviceMake(bidRequest.getDevice().getMake())
                     .bundle(bidRequest.getApp().getBundle())
                     .country(Optional.ofNullable(bidRequest.getDevice().getGeo())
                                      .map(Geo::getCountry)
                                      .orElse(null))
                     .creativeId(creativeId)
                     .bidFloor(Double.valueOf(imp.getBidfloor()))
                     .rtaFeature(Optional.ofNullable(adDTO.getCampaignRtaInfo())
                                         .map(CampaignRtaInfo::getRtaFeature)
                                         .orElse(null))
                     .packageName(adDTO.getCampaign().getPackageName())
                     .category(adDTO.getCampaign().getCategory())
                     .build();
  }

  public void calcPrice(Map<Integer, AdDTOWrapper> adDTOMap) {
    Params params = assignParams();
    try {
      ThreadPool.getInstance().execute(() -> {
        adDTOMap.values().forEach(e -> e.setBidPrice(doCalcPrice(e)));
        params.put(ParamKey.ADS_CALC_PRICE_RESPONSE, adDTOMap);
        messageQueue.putMessage(EventType.CALC_CPC_FINISH, params);
      });
    } catch (Exception e) {
      log.error("calculate cpc error: {}", e.getMessage());
      messageQueue.putMessage(EventType.CALC_CPC_ERROR);
    }
  }

  private double doCalcPrice(AdDTOWrapper adDTOWrapper) {
    // todo 根据策略有不同的计算方式
    AdDTO adDTO = adDTOWrapper.getAdDTO();
    Integer bidStrategy = adDTO.getAdGroup().getBidStrategy();
    // todo 确认下pctr是0.01还是1表示1%
    return adDTO.getAdGroup().getOptPrice() * adDTOWrapper.getPCtr() * 1000;
  }

  public void filerAdAndNotifySuccess(Map<Integer, AdDTOWrapper> adDTOMap) {

    adDTOMap = adDTOMap.values()
                       .stream()
                       .filter(e -> e.getBidPrice() > Optional.of(imp.getBidfloor()).orElse(0f))
                       .collect(Collectors.toMap(e -> e.getAdDTO().getAd().getId(), e -> e));
    Params params = assignParams().put(ParamKey.ADS_TASK_RESPONSE, adDTOMap);
    messageQueue.putMessage(EventType.BID_TASK_FINISH, params);

  }

  public void notifyFailed() {
    messageQueue.putMessage(EventType.BID_TASK_FAILED, assignParams());
  }

  public Params assignParams() {
    return Params.create(ParamKey.REQUEST_ID, requestId).put(ParamKey.TASK_ID, taskId);
  }
}
