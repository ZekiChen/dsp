package com.tecdo.fsm.task;

import com.ejlchina.data.TypeRef;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.OkHttps;
import com.tecdo.ab.util.AbTestConfigHelper;
import com.tecdo.common.domain.biz.R;
import com.tecdo.common.thread.ThreadPool;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.domain.biz.BidCreative;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.biz.request.CtrRequest;
import com.tecdo.domain.biz.response.CtrResponse;
import com.tecdo.domain.openrtb.request.Banner;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Device;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.AbTestConfig;
import com.tecdo.entity.Affiliate;
import com.tecdo.entity.CampaignRtaInfo;
import com.tecdo.entity.Creative;
import com.tecdo.entity.TargetCondition;
import com.tecdo.entity.doris.GooglePlayApp;
import com.tecdo.enums.biz.AdTypeEnum;
import com.tecdo.enums.biz.BidStrategyEnum;
import com.tecdo.filter.AbstractRecallFilter;
import com.tecdo.filter.factory.RecallFiltersFactory;
import com.tecdo.filter.util.FilterChainHelper;
import com.tecdo.fsm.task.state.ITaskState;
import com.tecdo.fsm.task.state.InitState;
import com.tecdo.service.init.AbTestConfigManager;
import com.tecdo.service.init.AdManager;
import com.tecdo.service.init.GooglePlayAppManager;
import com.tecdo.service.init.RtaInfoManager;
import com.tecdo.util.CreativeHelper;
import com.tecdo.util.FieldFormatHelper;
import com.tecdo.util.JsonHelper;

import org.apache.commons.collections.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
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
  private Long requestId;
  // taskId = bidId
  private String taskId;

  private String ctrPredictUrl = SpringUtil.getProperty("pac.ctr-predict.url");

  private final AdManager adManager = SpringUtil.getBean(AdManager.class);
  private final RtaInfoManager rtaInfoManager = SpringUtil.getBean(RtaInfoManager.class);
  private final AbTestConfigManager abTestConfigManager =
    SpringUtil.getBean(AbTestConfigManager.class);
  private final GooglePlayAppManager googlePlayAppManager =
    SpringUtil.getBean(GooglePlayAppManager.class);
  private final RecallFiltersFactory filtersFactory =
    SpringUtil.getBean(RecallFiltersFactory.class);
  private final ThreadPool threadPool = SpringUtil.getBean(ThreadPool.class);
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
      List<TargetCondition> conditions = listLegalCondition(adDTO.getConditions());
      // 原则上不应该修改adDTO，因为所有的请求都是直接用的manager的数据，但是conditions的过滤对所有请求都适用，在这里操作也没有实际影响
      adDTO.setConditions(conditions);
      // 该 AD 没有定投需求，过滤掉
      if (CollUtil.isEmpty(conditions)) {
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

  public void callCtr3Api(Map<Integer, AdDTOWrapper> adDTOMap) {
    Params params = assignParams();
    BidRequest bidRequest = this.bidRequest;
    Imp imp = this.imp;
    Integer affId = this.affiliate.getId();
    threadPool.execute(() -> {
      try {
        HttpResult httpResult = buildAndCallCtr3Api(adDTOMap, bidRequest, imp, affId);
        if (httpResult.isSuccessful()) {
          R<List<CtrResponse>> result =
            httpResult.getBody().toBean(new TypeRef<R<List<CtrResponse>>>() {
            });
          if (result == null || CollectionUtils.isEmpty(result.getData())) {
            log.error("taskId: {},ctr response unexpected result: {}", taskId, result);
            messageQueue.putMessage(EventType.CTR_PREDICT_ERROR, params);
            return;
          }
          for (CtrResponse resp : result.getData()) {
            AdDTOWrapper wrapper = adDTOMap.get(resp.getAdId());
            wrapper.setPCtr(resp.getPCtr());
            wrapper.setPCtrVersion(result.getVersion());
          }
          params.put(ParamKey.ADS_P_CTR_RESPONSE, adDTOMap);
          messageQueue.putMessage(EventType.CTR_PREDICT_FINISH, params);
        } else {
          log.error("taskId: {},ctr request status: {}, error:",
                    taskId,
                    httpResult.getStatus(),
                    httpResult.getError());
          messageQueue.putMessage(EventType.CTR_PREDICT_ERROR, params);
        }
      } catch (Exception e) {
        log.error("taskId: {},ctr request cause a exception", taskId, e);
        messageQueue.putMessage(EventType.CTR_PREDICT_ERROR, params);
      }
    });
  }

  private HttpResult buildAndCallCtr3Api(Map<Integer, AdDTOWrapper> adDTOMap,
                                         BidRequest bidRequest,
                                         Imp imp,
                                         Integer affId) {
    List<CtrRequest> ctrRequests = //
      adDTOMap.values()
              .stream()
              .map(adDTOWrapper -> buildCtrRequest(bidRequest, imp, affId, adDTOWrapper.getAdDTO()))
              .collect(Collectors.toList());
    Map<String, Object> paramMap =
      MapUtil.<String, Object>builder().put("data", ctrRequests).build();

    Map<String, List<AbTestConfig>> abTestConfigMap = abTestConfigManager.getAbTestConfigMap();
    String url = ctrPredictUrl;
    for (Map.Entry<String, List<AbTestConfig>> entry : abTestConfigMap.entrySet()) {
      String tag = entry.getKey();
      List<AbTestConfig> configList = entry.getValue();
      if (AbTestConfigHelper.execute(configList, bidRequest, affId)) {
        url = ctrPredictUrl + "/" + tag;
        break;
      }
    }
    return OkHttps.sync(url)
                  .bodyType(OkHttps.JSON)
                  .setBodyPara(JsonHelper.toJSONString(paramMap))
                  .post();
  }

  private CtrRequest buildCtrRequest(BidRequest bidRequest, Imp imp, Integer affId, AdDTO adDTO) {
    Integer creativeId = CreativeHelper.getCreativeId(adDTO.getAd());
    // 版位的
    BidCreative bidCreative = CreativeHelper.getAdFormat(imp);
    // 素材的
    Creative creative = adDTO.getCreativeMap().get(creativeId);
    AdTypeEnum adType = AdTypeEnum.of(adDTO.getAd().getType());
    Integer adWidth;
    Integer adHeight;
    if (adType == AdTypeEnum.BANNER) {
      // banner广告用素材大小
      adWidth = creative.getWidth();
      adHeight = creative.getHeight();
    } else {
      // native广告用native大小，因为native有wmin，hmin，所以用版位大小
      adWidth = Integer.parseInt(bidCreative.getWidth());
      adHeight = Integer.parseInt(bidCreative.getHeight());
    }
    Device device = bidRequest.getDevice();
    GooglePlayApp googleApp =
      googlePlayAppManager.getGoogleAppOrEmpty(bidRequest.getApp().getBundle());
    return CtrRequest.builder()
                     .adId(adDTO.getAd().getId())
                     .dayOld(DateUtil.today())
                     .affiliateId(affId)
                     .adFormat(adType.getDesc())
                     .adWidth(adWidth)
                     .adHeight(adHeight)
                     .os(FieldFormatHelper.osFormat(device.getOs()))
                     .osv(device.getOsv())
                     .deviceMake(FieldFormatHelper.deviceMakeFormat(device.getMake()))
                     .bundleId(FieldFormatHelper.bundleIdFormat(bidRequest.getApp().getBundle()))
                     .bundleOld(FieldFormatHelper.bundleIdFormat(bidRequest.getApp().getBundle()))
                     .country(FieldFormatHelper.countryFormat(device.getGeo().getCountry()))
                     .connectionType(device.getConnectiontype())
                     .deviceModel(FieldFormatHelper.deviceModelFormat(device.getModel()))
                     .carrier(device.getCarrier())
                     .creativeId(creativeId)
                     .bidFloor(Double.valueOf(imp.getBidfloor()))
                     .feature1(Optional.ofNullable(adDTO.getCampaignRtaInfo())
                                       .map(CampaignRtaInfo::getRtaFeature)
                                       .orElse(-1))
                     .rtaFeatureOld(Optional.ofNullable(adDTO.getCampaignRtaInfo())
                                            .map(CampaignRtaInfo::getRtaFeature)
                                            .orElse(-1))
                     .packageName(adDTO.getCampaign().getPackageName())
                     .packageNameOld(adDTO.getCampaign().getPackageName())
                     .category(adDTO.getCampaign().getCategory())
                     .pos(Optional.ofNullable(imp.getBanner()).map(Banner::getPos).orElse(0))
                     .domain(bidRequest.getApp().getDomain())
                     .instl(imp.getInstl())
                     .cat(bidRequest.getApp().getCat())
                     .ip(device.getIp())
                     .ua(device.getUa())
                     .lang(FieldFormatHelper.languageFormat(device.getLanguage()))
                     .deviceId(device.getIfa())
                     .categoryList(googleApp.getCategoryList())
                     .tagList(googleApp.getTagList())
                     .score(googleApp.getScore())
                     .downloads(googleApp.getDownloads())
                     .reviews(googleApp.getReviews())
                     .build();
  }

  public void calcPrice(Map<Integer, AdDTOWrapper> adDTOMap) {
    Params params = assignParams();
    try {
      adDTOMap.values().forEach(e -> e.setBidPrice(doCalcPrice(e)));
      params.put(ParamKey.ADS_CALC_PRICE_RESPONSE, adDTOMap);
      messageQueue.putMessage(EventType.CALC_CPC_FINISH, params);
    } catch (Exception e) {
      log.error("taskId: {},calculate cpc cause a exception", taskId, e);
      messageQueue.putMessage(EventType.CALC_CPC_ERROR);
    }
  }

  private BigDecimal doCalcPrice(AdDTOWrapper adDTOWrapper) {
    AdDTO adDTO = adDTOWrapper.getAdDTO();
    BidStrategyEnum bidStrategy = BidStrategyEnum.of(adDTO.getAdGroup().getBidStrategy());
    BigDecimal bidPrice;
    switch (bidStrategy) {
      case CPM:
        bidPrice = BigDecimal.valueOf(adDTO.getAdGroup().getOptPrice());
        break;
      case CPC:
        bidPrice = BigDecimal.valueOf(adDTO.getAdGroup().getOptPrice())
                             .multiply(BigDecimal.valueOf(adDTOWrapper.getPCtr()))
                             .multiply(BigDecimal.valueOf(1000));
        break;
      default:
        bidPrice = BigDecimal.valueOf(adDTO.getAdGroup().getOptPrice())
                             .multiply(BigDecimal.valueOf(adDTOWrapper.getPCtr()))
                             .multiply(BigDecimal.valueOf(1000));
    }
    return bidPrice;
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

  }

  public void notifyFailed() {
    messageQueue.putMessage(EventType.BID_TASK_FAILED, assignParams());
  }

  public Params assignParams() {
    return Params.create(ParamKey.REQUEST_ID, requestId).put(ParamKey.TASK_ID, taskId);
  }
}
