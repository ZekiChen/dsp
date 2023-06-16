package com.tecdo.fsm.task;

import com.dianping.cat.Cat;
import com.ejlchina.data.TypeRef;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.OkHttps;
import com.google.common.base.MoreObjects;
import com.tecdo.ab.util.AbTestConfigHelper;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.CampaignRtaInfo;
import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.adm.api.delivery.enums.BidStrategyEnum;
import com.tecdo.adm.api.doris.entity.BundleData;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.core.launch.response.R;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.domain.biz.BidCreative;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.biz.request.PredictRequest;
import com.tecdo.domain.biz.response.PredictResponse;
import com.tecdo.domain.openrtb.request.Banner;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Device;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.AbTestConfig;
import com.tecdo.entity.doris.GooglePlayApp;
import com.tecdo.filter.AbstractRecallFilter;
import com.tecdo.filter.factory.RecallFiltersFactory;
import com.tecdo.filter.util.FilterChainHelper;
import com.tecdo.fsm.task.state.ITaskState;
import com.tecdo.fsm.task.state.InitState;
import com.tecdo.service.CacheService;
import com.tecdo.service.init.AbTestConfigManager;
import com.tecdo.service.init.AdManager;
import com.tecdo.service.init.BundleDataManager;
import com.tecdo.service.init.GooglePlayAppManager;
import com.tecdo.service.init.RtaInfoManager;
import com.tecdo.util.ActionConsumeRecorder;
import com.tecdo.util.CreativeHelper;
import com.tecdo.util.FieldFormatHelper;
import com.tecdo.util.JsonHelper;

import org.apache.commons.collections.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
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

  private String ctrPredictUrl = SpringUtil.getProperty("pac.ctr-predict.url");
  private String cvrPredictUrl = SpringUtil.getProperty("pac.cvr-predict.url");
  private String cvrEvent1PredictUrl = SpringUtil.getProperty("pac.cvr-event1-predict.url");
  private String cvrEvent2PredictUrl = SpringUtil.getProperty("pac.cvr-event2-predict.url");
  private String cvrEvent3PredictUrl = SpringUtil.getProperty("pac.cvr-event3-predict.url");
  private String cvrEvent10PredictUrl = SpringUtil.getProperty("pac.cvr-event10-predict.url");
  private int needReceiveCount = 0;
  private int predictResCount = 0;
  private String multiplier = SpringUtil.getProperty("pac.bundle.test.multiplier");

  private final AdManager adManager = SpringUtil.getBean(AdManager.class);
  private final RtaInfoManager rtaInfoManager = SpringUtil.getBean(RtaInfoManager.class);
  private final AbTestConfigManager abTestConfigManager =
    SpringUtil.getBean(AbTestConfigManager.class);
  private final GooglePlayAppManager googlePlayAppManager =
    SpringUtil.getBean(GooglePlayAppManager.class);
  private final BundleDataManager bundleDataManager = SpringUtil.getBean(BundleDataManager.class);
  private final CacheService cacheService = SpringUtil.getBean(CacheService.class);
  private final RecallFiltersFactory filtersFactory =
    SpringUtil.getBean(RecallFiltersFactory.class);
  private final ThreadPool threadPool = SpringUtil.getBean(ThreadPool.class);
  private final SoftTimer softTimer = SpringUtil.getBean(SoftTimer.class);
  private final MessageQueue messageQueue = SpringUtil.getBean(MessageQueue.class);
  private final Map<EventType, Long> eventTimerMap = new HashMap<>();
  private final Map<Integer, AdDTOWrapper> resMap = new HashMap<>();

  private ActionConsumeRecorder recorder = new ActionConsumeRecorder();

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
    this.eventTimerMap.clear();
    this.currentState = SpringUtil.getBean(InitState.class);
    this.needReceiveCount = 0;
    this.predictResCount = 0;
    this.resMap.clear();
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

  public void listRecallAd() {
    Params params = assignParams();
    BidRequest bidRequest = this.bidRequest;
    Imp imp = this.imp;
    Affiliate affiliate = this.affiliate;
    threadPool.execute(() -> {
      try {
        params.put(ParamKey.ADS_RECALL_RESPONSE, doListRecallAd(bidRequest, imp, affiliate));
        messageQueue.putMessage(EventType.ADS_RECALL_FINISH, params);
      } catch (Exception e) {
        log.error(
          "taskId: {},list recall ad error,  so this request will not participate in bidding",
          taskId,
          e);
        messageQueue.putMessage(EventType.ADS_RECALL_ERROR, params);
      }
    });
  }

  /**
   * 广告召回
   */
  private Map<Integer, AdDTOWrapper> doListRecallAd(BidRequest bidRequest,
                                                    Imp imp,
                                                    Affiliate affiliate) {
    List<AbstractRecallFilter> filters = filtersFactory.createFilters();
    FilterChainHelper.assemble(filters);

    Map<Integer, AdDTOWrapper> resMap = new HashMap<>();
    for (AdDTO adDTO : adManager.getAdDTOMap().values()) {
      // 该 AD 没有定投需求，过滤掉
      if (CollUtil.isEmpty(adDTO.getConditions())) {
        log.warn("ad: {} doesn't have condition, filter", adDTO.getAd().getId());
        continue;
      }
      // 有定投需求，校验：每个 AD 都需要被所有 filter 判断一遍
      if (FilterChainHelper.executeFilter(filters.get(0), adDTO, bidRequest, imp, affiliate)) {
        // when timeout,imp will set to null,then imp.getId() will cause null point exception
        resMap.put(adDTO.getAd().getId(), new AdDTOWrapper(imp.getId(), taskId, adDTO));
      }
    }
    return resMap;
  }


  public void callPredictApi(Map<Integer, AdDTOWrapper> adDTOMap) {

    Map<Integer, AdDTOWrapper> cpcMap = new HashMap<>();
    Map<Integer, AdDTOWrapper> cpaMap = new HashMap<>();
    Map<Integer, AdDTOWrapper> cpa1Map = new HashMap<>();
    Map<Integer, AdDTOWrapper> cpa2Map = new HashMap<>();
    Map<Integer, AdDTOWrapper> cpa3Map = new HashMap<>();
    Map<Integer, AdDTOWrapper> cpa10Map = new HashMap<>();
    Map<Integer, AdDTOWrapper> noNeedPredict = new HashMap<>();
    adDTOMap.forEach((k, v) -> {
      BidStrategyEnum strategyEnum = BidStrategyEnum.of(v.getAdDTO().getAdGroup().getBidStrategy());
      switch (strategyEnum) {
        case CPC:
          cpcMap.put(k, v);
          break;
        case CPA:
          cpaMap.put(k, v);
          break;
        case CPA_EVENT1:
          cpcMap.put(k, v);
          cpa1Map.put(k, v);
          break;
        case CPA_EVENT2:
          cpcMap.put(k, v);
          cpa2Map.put(k, v);
          break;
        case CPA_EVENT3:
          cpcMap.put(k, v);
          cpa3Map.put(k, v);
          break;
        case CPA_EVENT10:
          cpcMap.put(k, v);
          cpa10Map.put(k, v);
          break;
        case CPM:
        case DYNAMIC:
        default:
          noNeedPredict.put(k, v);
      }
    });
    callAndMetricPredict(cpcMap, "ctr-batch-size", ctrPredictUrl);
    callAndMetricPredict(cpaMap, "cvr-batch-size", cvrPredictUrl);
    callAndMetricPredict(cpa1Map, "cvr-event1-batch-size", cvrEvent1PredictUrl);
    callAndMetricPredict(cpa2Map, "cvr-event2-batch-size", cvrEvent2PredictUrl);
    callAndMetricPredict(cpa3Map, "cvr-event3-batch-size", cvrEvent3PredictUrl);
    callAndMetricPredict(cpa10Map, "cvr-event10-batch-size", cvrEvent10PredictUrl);

    this.needReceiveCount++;
    messageQueue.putMessage(EventType.PREDICT_FINISH,
                            assignParams().put(ParamKey.ADS_PREDICT_RESPONSE, noNeedPredict));
  }

  private void callAndMetricPredict(Map<Integer, AdDTOWrapper> adDTOMap,
                                    String metric,
                                    String predictUrl) {

    if (!adDTOMap.isEmpty()) {
      this.needReceiveCount++;
      BidRequest bidRequest = this.bidRequest;
      Imp imp = this.imp;
      Integer affId = this.affiliate.getId();
      Cat.logMetricForDuration(metric, adDTOMap.size());
      Params params = assignParams();
      threadPool.execute(() -> doCallPredict(adDTOMap, params, bidRequest, imp, affId, predictUrl));
    }
  }

  private void doCallPredict(Map<Integer, AdDTOWrapper> adDTOMap,
                             Params params,
                             BidRequest bidRequest,
                             Imp imp,
                             Integer affId,
                             String predictUrl) {
    try {
      HttpResult httpResult = buildAndCallPredictApi(adDTOMap, bidRequest, imp, affId, predictUrl);
      if (httpResult.isSuccessful()) {
        R<List<PredictResponse>> result =
          httpResult.getBody().toBean(new TypeRef<R<List<PredictResponse>>>() {
          });
        if (result == null || CollectionUtils.isEmpty(result.getData())) {
          log.error("taskId: {},predict response unexpected result: {}", taskId, result);
          messageQueue.putMessage(EventType.PREDICT_ERROR, params);
          return;
        }
        for (PredictResponse resp : result.getData()) {
          AdDTOWrapper wrapper = adDTOMap.get(resp.getAdId());
          if (resp.getPCtr() != null) {
            wrapper.setPCtr(resp.getPCtr());
            wrapper.setPCtrVersion(result.getVersion());
          }
          if (resp.getPCvr() != null) {
            wrapper.setPCvr(resp.getPCvr());
            wrapper.setPCvrVersion(result.getVersion());
          }
        }
        params.put(ParamKey.ADS_PREDICT_RESPONSE, adDTOMap);
        messageQueue.putMessage(EventType.PREDICT_FINISH, params);
      } else {
        log.error("taskId: {},predict request status: {}, error:",
                  taskId,
                  httpResult.getStatus(),
                  httpResult.getError());
        messageQueue.putMessage(EventType.PREDICT_ERROR, params);
      }
    } catch (Exception e) {
      log.error("taskId: {},predict request cause a exception", taskId, e);
      messageQueue.putMessage(EventType.PREDICT_ERROR, params);
    }
  }

  private HttpResult buildAndCallPredictApi(Map<Integer, AdDTOWrapper> adDTOMap,
                                            BidRequest bidRequest,
                                            Imp imp,
                                            Integer affId,
                                            String predictUrl) {
    List<PredictRequest> predictRequests = //
      adDTOMap.values()
              .stream()
              .map(adDTOWrapper -> buildPredictRequest(bidRequest,
                                                       imp,
                                                       affId,
                                                       adDTOWrapper.getAdDTO()))
              .collect(Collectors.toList());
    Map<String, Object> paramMap =
      MapUtil.<String, Object>builder().put("data", predictRequests).build();

    Map<String, List<AbTestConfig>> abTestConfigMap = abTestConfigManager.getAbTestConfigMap();
    String url = predictUrl;
    for (Map.Entry<String, List<AbTestConfig>> entry : abTestConfigMap.entrySet()) {
      List<AbTestConfig> configList = entry.getValue();
      if (AbTestConfigHelper.execute(configList, bidRequest, affId)) {
        AbTestConfig config = configList.get(0);
        if (config.getWeight() > ThreadLocalRandom.current().nextDouble(100)) {
          url = predictUrl + "/" + config.getPath();
          break;
        }
      }
    }
    return OkHttps.sync(url)
                  .bodyType(OkHttps.JSON)
                  .setBodyPara(JsonHelper.toJSONString(paramMap))
                  .post();
  }

  private PredictRequest buildPredictRequest(BidRequest bidRequest,
                                             Imp imp,
                                             Integer affId,
                                             AdDTO adDTO) {
    Integer creativeId = CreativeHelper.getCreativeId(adDTO.getAd());
    // 版位的
    BidCreative bidCreative = CreativeHelper.getAdFormat(imp);
    // 素材的
    Creative creative = adDTO.getCreativeMap().get(creativeId);
    AdTypeEnum adType = AdTypeEnum.of(adDTO.getAd().getType());
    // 用版位大小
    Integer adWidth = Integer.parseInt(bidCreative.getWidth());
    Integer adHeight = Integer.parseInt(bidCreative.getHeight());

    Device device = bidRequest.getDevice();
    GooglePlayApp googleApp =
      googlePlayAppManager.getGoogleAppOrEmpty(bidRequest.getApp().getBundle());
    return PredictRequest.builder()
                         .adId(adDTO.getAd().getId())
                         .affiliateId(affId)
                         .adFormat(adType.getDesc())
                         .adWidth(adWidth)
                         .adHeight(adHeight)
                         .os(FieldFormatHelper.osFormat(device.getOs()))
                         .osv(device.getOsv())
                         .deviceMake(FieldFormatHelper.deviceMakeFormat(device.getMake()))
                         .bundleId(FieldFormatHelper.bundleIdFormat(bidRequest.getApp()
                                                                              .getBundle()))
                         .country(FieldFormatHelper.countryFormat(device.getGeo().getCountry()))
                         .connectionType(device.getConnectiontype())
                         .deviceModel(FieldFormatHelper.deviceModelFormat(device.getModel()))
                         .carrier(device.getCarrier())
                         .creativeId(creativeId)
                         .bidFloor(Double.valueOf(imp.getBidfloor()))
                         .feature1(Optional.ofNullable(adDTO.getCampaignRtaInfo())
                                           .map(CampaignRtaInfo::getRtaFeature)
                                           .orElse(-1))
                         .packageName(adDTO.getCampaign().getPackageName())
                         .category(adDTO.getCampaign().getCategory())
                         .pos(Optional.ofNullable(imp.getBanner()).map(Banner::getPos).orElse(0))
                         .domain(bidRequest.getApp().getDomain())
                         .instl(imp.getInstl())
                         .cat(bidRequest.getApp().getCat())
                         .ip(device.getIp())
                         .ua(device.getUa())
                         .lang(FieldFormatHelper.languageFormat(device.getLanguage()))
                         .deviceId(device.getIfa())
                         .bundleIdCategory(googleApp.getCategoryList())
                         .bundleIdTag(googleApp.getTagList())
                         .bundleIdScore(googleApp.getScore())
                         .bundleIdDownload(googleApp.getDownloads())
                         .bundleIdReview(googleApp.getReviews())
                         .tagId(imp.getTagid())
                         .build();
  }

  public void savePredictResponse(Map<Integer, AdDTOWrapper> adDTOMap) {
    this.resMap.putAll(adDTOMap);
    this.predictResCount++;
  }

  public boolean isReceiveAllPredictResponse() {
    return predictResCount == needReceiveCount;
  }

  public void calcPrice() {
    Params params = assignParams();
    try {
      String key = makeKey();

      this.resMap.values().forEach(e -> e.setBidPrice(doCalcPrice(e, key)));
      params.put(ParamKey.ADS_CALC_PRICE_RESPONSE, this.resMap);
      messageQueue.putMessage(EventType.CALC_CPC_FINISH, params);
    } catch (Exception e) {
      log.error("taskId: {},calculate cpc cause a exception", taskId, e);
      messageQueue.putMessage(EventType.CALC_CPC_ERROR);
    }
  }

  private String makeKey() {
    Device device = bidRequest.getDevice();
    BidCreative bidCreative = CreativeHelper.getAdFormat(imp);
    return FieldFormatHelper.countryFormat(device.getGeo().getCountry())
                            .concat("_")
                            .concat(FieldFormatHelper.bundleIdFormat(bidRequest.getApp()
                                                                               .getBundle()))
                            .concat("_")
                            .concat(Optional.ofNullable(bidCreative.getType())
                                            .map(AdTypeEnum::of)
                                            .map(AdTypeEnum::getDesc)
                                            .orElse(""))
                            .concat("_")
                            .concat(MoreObjects.firstNonNull(bidCreative.getWidth(), ""))
                            .concat("_")
                            .concat(MoreObjects.firstNonNull(bidCreative.getHeight(), ""));
  }

  private BigDecimal doCalcPrice(AdDTOWrapper adDTOWrapper, String key) {
    BigDecimal finalPrice;
    AdDTO adDTO = adDTOWrapper.getAdDTO();
    BundleData bundleData = bundleDataManager.getBundleData(key);
    boolean needTest = !bundleDataManager.isImpGtSize(key);
    if (needTest && adDTO.getAdGroup().getBundleTestEnable() && bundleData != null) {
      Double winRate = bundleData.getWinRate();
      Double bidPrice = bundleData.getBidPrice();
      if (winRate < affiliate.getRequireWinRate()) {
        if (bidPrice < imp.getBidfloor()) {
          finalPrice = BigDecimal.valueOf(imp.getBidfloor()).multiply(new BigDecimal(multiplier));
        } else {
          finalPrice = BigDecimal.valueOf(bidPrice).multiply(new BigDecimal(multiplier));
        }
      } else {
        finalPrice = BigDecimal.valueOf(bidPrice)
                               .multiply(BigDecimal.valueOf(bundleData.getK()))
                               .divide(BigDecimal.valueOf(bundleData.getOldK()),
                                       RoundingMode.HALF_UP);
      }
    } else {
      BidStrategyEnum bidStrategy = BidStrategyEnum.of(adDTO.getAdGroup().getBidStrategy());
      switch (bidStrategy) {
        case CPC:
          finalPrice = BigDecimal.valueOf(adDTO.getAdGroup().getOptPrice())
                                 .multiply(BigDecimal.valueOf(adDTOWrapper.getPCtr()))
                                 .multiply(BigDecimal.valueOf(1000));
          break;
        case CPA:
          finalPrice = BigDecimal.valueOf(adDTO.getAdGroup().getOptPrice())
                                 .multiply(BigDecimal.valueOf(adDTOWrapper.getPCvr()))
                                 .multiply(BigDecimal.valueOf(1000));
          break;
        case DYNAMIC:
          ThreadLocalRandom random = ThreadLocalRandom.current();
          if (adDTO.getAdGroup().getBidProbability() > random.nextDouble(100)) {
            finalPrice = BigDecimal.valueOf(adDTO.getAdGroup().getBidMultiplier())
                                   .multiply(BigDecimal.valueOf(imp.getBidfloor()))
                                   .min(BigDecimal.valueOf(adDTO.getAdGroup().getOptPrice()));
          } else {
            finalPrice = BigDecimal.ZERO;
          }
          break;
        case CPA_EVENT1:
        case CPA_EVENT2:
        case CPA_EVENT3:
        case CPA_EVENT10:
          finalPrice = BigDecimal.valueOf(adDTO.getAdGroup().getOptPrice())
                                 .multiply(BigDecimal.valueOf(adDTOWrapper.getPCtr()))
                                 .multiply(BigDecimal.valueOf(adDTOWrapper.getPCvr()))
                                 .multiply(BigDecimal.valueOf(1000));
          break;
        case CPM:
        default:
          finalPrice = BigDecimal.valueOf(adDTO.getAdGroup().getOptPrice());
      }
    }
    return finalPrice;
  }

  public void filerAdAndNotifySuccess(Map<Integer, AdDTOWrapper> adDTOMap) {
    // 过滤掉出价低于底价的广告
    adDTOMap = adDTOMap.values()
                       .stream()
                       .filter(e -> e.getBidPrice()
                                     .compareTo(BigDecimal.valueOf(Optional.of(imp.getBidfloor())
                                                                           .orElse(0f))) > 0)
                       .collect(Collectors.toMap(e -> e.getAdDTO().getAd().getId(), e -> e));
    Params params = assignParams().put(ParamKey.ADS_TASK_RESPONSE, adDTOMap);
    messageQueue.putMessage(EventType.BID_TASK_FINISH, params);
    record();
  }

  public void notifyFailed() {
    record();
    messageQueue.putMessage(EventType.BID_TASK_FAILED, assignParams());
  }

  public Params assignParams() {
    return Params.create(ParamKey.REQUEST_ID, requestId).put(ParamKey.TASK_ID, taskId);
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
}
