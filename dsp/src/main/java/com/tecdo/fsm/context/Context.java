package com.tecdo.fsm.context;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.tecdo.adm.api.delivery.entity.Ad;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.RtaInfo;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.adm.api.delivery.enums.AdvEnum;
import com.tecdo.common.constant.HttpCode;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.FormatKey;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.biz.notice.NoticeInfo;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.domain.openrtb.response.Bid;
import com.tecdo.domain.openrtb.response.BidResponse;
import com.tecdo.domain.openrtb.response.SeatBid;
import com.tecdo.domain.openrtb.response.n.NativeResponse;
import com.tecdo.domain.openrtb.response.n.NativeResponseWrapper;
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
import com.tecdo.service.rta.ae.AeMaterialTypeEnum;
import com.tecdo.service.rta.ae.AeRtaInfoVO;
import com.tecdo.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
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

  private String winUrl = SpringUtil.getProperty("pac.notice.win-url");
  private String impUrl = SpringUtil.getProperty("pac.notice.imp-url");
  private String clickUrl = SpringUtil.getProperty("pac.notice.click-url");

  private final String AUCTION_PRICE_PARAM = "&bid_success_price=${AUCTION_PRICE}";


  private Map<String, Task> taskMap = new HashMap<>();
  // taskId,adId,AdDTOWrapper
  private Map<String, Map<Integer, AdDTOWrapper>> taskResponse = new HashMap<>();

  private List<AdDTOWrapper> adDTOWrapperList = new ArrayList<>();

  private Map<EventType, Long> eventTimerMap = new HashMap<>();

  private final MessageQueue messageQueue = SpringUtil.getBean(MessageQueue.class);

  private final SoftTimer softTimer = SpringUtil.getBean(SoftTimer.class);

  private final ThreadPool threadPool = SpringUtil.getBean(ThreadPool.class);

  private final TaskPool taskPool = SpringUtil.getBean(TaskPool.class);

  private final RtaInfoManager rtaInfoManager = SpringUtil.getBean(RtaInfoManager.class);

  public int  rtaResponseCount = 0;
  public final int rtaResponseNeed = 2;

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

  }

  public void handleBidRequest() {
    String bidRequestString = JsonHelper.toJSONString(bidRequest);
    log.info("contextId: {}, bid request is:{}", requestId, bidRequestString);
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
      target.setTarget(vo.getTarget() && AeMaterialTypeEnum.DPA.getDesc().equals(vo.getMaterialType()));
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
      BidResponse bidResponse = buildResponse(this.response);
      String bidResponseString = JsonHelper.toJSONString(bidResponse);
      log.info("contextId: {}, bid response is:{}", requestId, bidResponseString);
      params.put(ParamKey.RESPONSE_BODY, bidResponseString);
      params.put(ParamKey.HTTP_CODE, HttpCode.OK);
      params.put(ParamKey.CHANNEL_CONTEXT, httpRequest.getChannelContext());
    }
    messageQueue.putMessage(eventType, params);
  }

  private BidResponse buildResponse(AdDTOWrapper wrapper) {
    AdDTO adDTO = wrapper.getAdDTO();
    String bidId = wrapper.getBidId();
    BidResponse bidResponse = new BidResponse();
    bidResponse.setId(bidRequest.getId());
    bidResponse.setBidid(bidId);
    Bid bid = new Bid();
    bid.setId(bidId);
    bid.setImpid(wrapper.getImpId());
    bid.setPrice(wrapper.getBidPrice().floatValue());
    String sign = SignHelper.digest(bidId, adDTO.getCampaign().getId().toString());
    String winUrl = urlFormat(getWinNoticeUrl()) + AUCTION_PRICE_PARAM;
    bid.setNurl(SignHelper.urlAddSign(winUrl, sign));
    bid.setAdm(buildAdm(wrapper));
    bid.setAdid(String.valueOf(adDTO.getAd().getId()));
    bid.setAdomain(Collections.singletonList(adDTO.getCampaign().getDomain()));
    bid.setBundle(adDTO.getCampaign().getPackageName());
    bid.setIurl(adDTO.getCreativeMap().get(CreativeHelper.getCreativeId(adDTO.getAd())).getUrl());
    bid.setCid(String.valueOf(adDTO.getCampaign().getId()));
    bid.setCrid(String.valueOf(CreativeHelper.getCreativeId(adDTO.getAd())));

    SeatBid seatBid = new SeatBid();
    seatBid.setBid(Collections.singletonList(bid));
    bidResponse.setSeatbid(Collections.singletonList(seatBid));

    return bidResponse;
  }

  private String buildAdm(AdDTOWrapper wrapper) {
    AdDTO adDTO = wrapper.getAdDTO();
    String adm = null;
    String impTrackUrls = adDTO.getAdGroup().getImpTrackUrls();
    List<String> impTrackList = new ArrayList<>();
    String systemImpTrack = getSystemImpTrack() + AUCTION_PRICE_PARAM;
    String sign = SignHelper.digest(wrapper.getBidId(), adDTO.getCampaign().getId().toString());
    impTrackList.add(SignHelper.urlAddSign(systemImpTrack, sign));
    if (impTrackUrls != null) {
      String[] split = impTrackUrls.split(",");
      impTrackList.addAll(Arrays.asList(split));
    }
    impTrackList = impTrackList.stream().map(i -> urlFormat(i, sign)).collect(Collectors.toList());

    String clickTrackUrls = adDTO.getAdGroup().getClickTrackUrls();
    List<String> clickTrackList = new ArrayList<>();
    String systemClickTrack = getSystemClickTrack();
    clickTrackList.add(SignHelper.urlAddSign(systemClickTrack, sign));
    if (clickTrackUrls != null) {
      String[] split = clickTrackUrls.split(",");
      clickTrackList.addAll(Arrays.asList(split));
    }
    clickTrackList =
      clickTrackList.stream().map(i -> urlFormat(i, sign)).collect(Collectors.toList());

    String clickUrl = StrUtil.isNotBlank(wrapper.getLandingPage()) ?
            wrapper.getLandingPage() :
            urlFormat(adDTO.getAdGroup().getClickUrl(), sign);
    String deeplink = urlFormat(adDTO.getAdGroup().getDeeplink(), sign);
    if (Objects.equals(adDTO.getAd().getType(), AdTypeEnum.BANNER.getType())) {
      adm = AdmGenerator.bannerAdm(clickUrl,
                                   deeplink,
                                   adDTO.getCreativeMap().get(adDTO.getAd().getImage()).getUrl(),
                                   impTrackList,
                                   clickTrackList);
    } else if (Objects.equals(adDTO.getAd().getType(), AdTypeEnum.NATIVE.getType())) {
      List<Imp> impList = bidRequest.getImp();
      Imp imp = impList.stream()
                       .filter(i -> Objects.equals(i.getId(), wrapper.getImpId()))
                       .findFirst()
                       .get();
      NativeResponse nativeResponse = //
        AdmGenerator.nativeAdm(imp.getNative1().getNativeRequest(),
                               adDTO,
                               clickUrl,
                               deeplink,
                               impTrackList,
                               clickTrackList);
      if (imp.getNative1().getNativeRequestWrapper() != null) {
        NativeResponseWrapper nativeResponseWrapper = new NativeResponseWrapper();
        nativeResponseWrapper.setNativeResponse(nativeResponse);
        adm = JsonHelper.toJSONString(nativeResponseWrapper);
      } else {
        adm = JsonHelper.toJSONString(nativeResponse);
      }
    }
    return adm;
  }

  private String urlFormat(String url, String sign) {
    if (url == null) {
      return null;
    }
    if (sign != null) {
      url = url.replace(FormatKey.SIGN, sign);
    }
    url = urlFormat(url);
    return url;
  }

  private String urlFormat(String url) {
    if (url == null) {
      return null;
    }
    url = url.replace(FormatKey.BID_ID, response.getBidId())
             .replace(FormatKey.IMP_ID, response.getImpId())
             .replace(FormatKey.CAMPAIGN_ID,
                      String.valueOf(response.getAdDTO().getCampaign().getId()))
             .replace(FormatKey.AFFILIATE_ID, String.valueOf(affiliate.getId()))
             .replace(FormatKey.AD_GROUP_ID,
                      String.valueOf(response.getAdDTO().getAdGroup().getId()))
             .replace(FormatKey.AD_ID, String.valueOf(response.getAdDTO().getAd().getId()))
             .replace(FormatKey.CREATIVE_ID,
                      String.valueOf(CreativeHelper.getCreativeId(response.getAdDTO().getAd())))
             .replace(FormatKey.DEVICE_ID, bidRequest.getDevice().getIfa())
             .replace(FormatKey.IP, encode(bidRequest.getDevice().getIp()))
             .replace(FormatKey.COUNTRY, bidRequest.getDevice().getGeo().getCountry())
             .replace(FormatKey.OS, bidRequest.getDevice().getOs())
             .replace(FormatKey.DEVICE_MAKE, encode(bidRequest.getDevice().getMake()))
             .replace(FormatKey.DEVICE_MODEL, encode(bidRequest.getDevice().getModel()))
             .replace(FormatKey.AD_FORMAT,
                      AdTypeEnum.of(response.getAdDTO().getAd().getType()).getDesc())
             .replace(FormatKey.BUNDLE, encode(bidRequest.getApp().getBundle()))
             .replace(FormatKey.RTA_TOKEN,
                      encode(StringUtils.firstNonEmpty(response.getRtaToken(), "")));
    return url;
  }

  private String encode(Object content) {
    if (content == null) {
      return "";
    }
    try {
      return URLEncoder.encode(content.toString(), "utf-8");
    } catch (Exception e) {
      return "";
    }
  }

  private String getWinNoticeUrl() {
    return winUrl;
  }

  private String getSystemImpTrack() {
    return impUrl;
  }

  private String getSystemClickTrack() {
    return clickUrl;
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
