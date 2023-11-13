package com.tecdo.fsm.task.handler;

import cn.hutool.core.map.MapUtil;
import com.dianping.cat.Cat;
import com.ejlchina.data.TypeRef;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.OkHttps;
import com.tecdo.ab.util.AbTestConfigHelper;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.CampaignRtaInfo;
import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.adm.api.delivery.enums.BidStrategyEnum;
import com.tecdo.adm.api.doris.entity.GooglePlayApp;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
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
import com.tecdo.service.init.AbTestConfigManager;
import com.tecdo.service.init.doris.GooglePlayAppManager;
import com.tecdo.transform.IProtoTransform;
import com.tecdo.transform.ResponseTypeEnum;
import com.tecdo.util.CreativeHelper;
import com.tecdo.util.FieldFormatHelper;
import com.tecdo.util.JsonHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * CTR/CVR 预估
 * <p>
 * Created by Zeki on 2023/9/22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PredictHandler {

    @Value("${pac.ctr-predict.url}")
    private String ctrPredictUrl;
    @Value("${pac.cvr-predict.url}")
    private String cvrPredictUrl;
    @Value("${pac.cvr-event1-predict.url}")
    private String cvrEvent1PredictUrl;
    @Value("${pac.cvr-event2-predict.url}")
    private String cvrEvent2PredictUrl;
    @Value("${pac.cvr-event3-predict.url}")
    private String cvrEvent3PredictUrl;
    @Value("${pac.cvr-event10-predict.url}")
    private String cvrEvent10PredictUrl;
    @Value("${pac.cvr-event11-predict.url}")
    private String cvrEvent11PredictUrl;

    @Value("${pac.force-jump.pctr}")
    private Double forceJumpPCtr;

    private final ThreadPool threadPool;
    private final MessageQueue messageQueue;

    private final AbTestConfigManager abTestConfigManager;
    private final GooglePlayAppManager googlePlayAppManager;

    public int callPredictApi(Map<Integer, AdDTOWrapper> adDTOMap, Params params,
                              BidRequest bidRequest, Imp imp, Affiliate affiliate, IProtoTransform protoTransform) {
        Map<Integer, AdDTOWrapper> noNeedPredict = new HashMap<>();
        Map<BidStrategyEnum, String> strategyUrlMap = new HashMap<>();
        strategyUrlMap.put(BidStrategyEnum.CPC, ctrPredictUrl);
        strategyUrlMap.put(BidStrategyEnum.CPA, cvrPredictUrl);
        strategyUrlMap.put(BidStrategyEnum.CPA_EVENT1, cvrEvent1PredictUrl);
        strategyUrlMap.put(BidStrategyEnum.CPA_EVENT2, cvrEvent2PredictUrl);
        strategyUrlMap.put(BidStrategyEnum.CPA_EVENT3, cvrEvent3PredictUrl);
        strategyUrlMap.put(BidStrategyEnum.CPA_EVENT10, cvrEvent10PredictUrl);
        strategyUrlMap.put(BidStrategyEnum.CPS, cvrEvent11PredictUrl);

        Map<BidStrategyEnum, Map<Integer, AdDTOWrapper>> strategyAdMap = new HashMap<>();
        for (BidStrategyEnum strategy : BidStrategyEnum.values()) {
            strategyAdMap.put(strategy, new HashMap<>());
        }

        for (Map.Entry<Integer, AdDTOWrapper> entry : adDTOMap.entrySet()) {
            Integer adId = entry.getKey();
            AdDTOWrapper w = entry.getValue();
            BidStrategyEnum strategy = BidStrategyEnum.of(w.getAdDTO().getAdGroup().getBidStrategy());
            switch (strategy) {
                case CPC:
                case CPA:
                    strategyAdMap.get(strategy).put(adId, w);
                    break;
                case CPA_EVENT1:
                case CPA_EVENT2:
                case CPA_EVENT3:
                case CPA_EVENT10:
                    strategyAdMap.get(BidStrategyEnum.CPC).put(adId, w);
                    strategyAdMap.get(strategy).put(adId, w);
                    break;
                case CPS:
                    String forceLink = w.getAdDTO().getAdGroup().getForceLink();
                    if (ResponseTypeEnum.FORCE.equals(protoTransform.getResponseType(forceLink, w))) {
                        w.setPCtr(forceJumpPCtr);
                    } else {
                        strategyAdMap.get(BidStrategyEnum.CPC).put(adId, w);
                    }
                    strategyAdMap.get(strategy).put(adId, w);
                    break;
                case CPM:
                case DYNAMIC:
                default:
                    noNeedPredict.put(adId, w);
            }
        }

        int needReceiveCount = 0;
        for (Map.Entry<BidStrategyEnum, Map<Integer, AdDTOWrapper>> entry : strategyAdMap.entrySet()) {
            needReceiveCount += callAndMetricPredict(entry.getValue(),
                    entry.getKey().name() + "-batch-size",
                    strategyUrlMap.get(entry.getKey()),
                    params, bidRequest, imp, affiliate);
        }

        messageQueue.putMessage(EventType.PREDICT_FINISH,
                params.put(ParamKey.ADS_PREDICT_RESPONSE, noNeedPredict));
        return ++needReceiveCount;
    }

    private int callAndMetricPredict(Map<Integer, AdDTOWrapper> adDTOMap,
                                     String metric, String predictUrl,
                                     Params params, BidRequest bidRequest, Imp imp, Affiliate affiliate) {

        if (!adDTOMap.isEmpty()) {
            Cat.logMetricForDuration(metric, adDTOMap.size());
            threadPool.execute(() -> doCallPredict(adDTOMap, params, bidRequest, imp, affiliate.getId(), predictUrl));
            return 1;
        }
        return 0;
    }

    private void doCallPredict(Map<Integer, AdDTOWrapper> adDTOMap,
                               Params params,
                               BidRequest bidRequest,
                               Imp imp,
                               Integer affId,
                               String predictUrl) {
        String taskId = params.get(ParamKey.TASK_ID);
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
                                adDTOWrapper))
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
                                               AdDTOWrapper wrapper) {
        AdDTO adDTO = wrapper.getAdDTO();
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
                .ip(Optional.ofNullable(device.getIp()).orElse(device.getIpv6()))
                .ua(device.getUa())
                .lang(FieldFormatHelper.languageFormat(device.getLanguage()))
                .deviceId(device.getIfa())
                .bundleIdCategory(googleApp.getCategoryList())
                .bundleIdTag(googleApp.getTagList())
                .bundleIdScore(googleApp.getScore())
                .bundleIdDownload(googleApp.getDownloads())
                .bundleIdReview(googleApp.getReviews())
                .tagId(imp.getTagid())
                .impFrequency(wrapper.getImpFrequency())
                .clickFrequency(wrapper.getClickFrequency())
                .build();
    }
}
