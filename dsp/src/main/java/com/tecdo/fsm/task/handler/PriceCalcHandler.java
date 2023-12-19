package com.tecdo.fsm.task.handler;

import com.ejlchina.data.TypeRef;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.OkHttps;
import com.google.common.base.MoreObjects;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.enums.*;
import com.tecdo.adm.api.doris.entity.BundleData;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.core.launch.response.R;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.domain.biz.BidCreative;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.biz.request.BidPriceInfo;
import com.tecdo.domain.biz.request.BidPriceRequest;
import com.tecdo.domain.biz.response.BidPriceResponse;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Device;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.service.BidPriceService;
import com.tecdo.service.init.doris.BundleDataManager;
import com.tecdo.transform.ProtoTransformFactory;
import com.tecdo.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 策略算价
 * <p>
 * Created by Zeki on 2023/9/22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceCalcHandler {

    private final ThreadPool threadPool;
    private final MessageQueue messageQueue;
    private final BidPriceService bidPriceService;
    private final BundleDataManager bundleDataManager;

    @Value("${pac.bid-price.learning.url}")
    private String learningBidUrl;
    @Value("${pac.bid-price.learning.enabled}")
    private Boolean learningBidEnable;
    @Value("${pac.bundle.test.multiplier}")
    private BigDecimal multiplier;
    @Value("${pac.bid-price.max-limit}")
    private BigDecimal maxPrice;
    @Value("${pac.bid-price.ecpx.enabled}")
    private Boolean eCPXBidEnable;

    public void calcPrice(Params params, Map<Integer, AdDTOWrapper> afterPredictAdMap,
                          BidRequest bidRequest, Imp imp, Affiliate affiliate) {
        String taskId = params.get(ParamKey.TASK_ID);
        try {
            Map<Integer, AdDTOWrapper> learningCalcAdMap = new HashMap<>();
            Map<Integer, AdDTOWrapper> generalCalcAdMap = new HashMap<>();
            groupCalcAdMap(afterPredictAdMap, learningCalcAdMap, generalCalcAdMap);
            params.put(ParamKey.ADS_CALC_PRICE_RESPONSE, afterPredictAdMap);

            handleGeneralCalcAd(params, bidRequest, imp, affiliate, taskId, generalCalcAdMap);
            handleLearningCalcAd(params, bidRequest, affiliate, taskId, learningCalcAdMap);
        } catch (Exception e) {
            log.error("taskId: {},calculate cpc cause a exception", taskId, e);
            messageQueue.putMessage(EventType.CALC_CPC_ERROR, params);
        }
    }

    private void groupCalcAdMap(Map<Integer, AdDTOWrapper> afterPredictAdMap,
                                Map<Integer, AdDTOWrapper> learningCalcAdMap,
                                Map<Integer, AdDTOWrapper> generalCalcAdMap) {
        afterPredictAdMap.values().forEach(w -> groupByBidAlgorithm(learningCalcAdMap, generalCalcAdMap,
                w, w.getBidStrategyEnum().getType(), w.getBidAlgorithmEnum().getType()));
    }

    private void groupByBidAlgorithm(Map<Integer, AdDTOWrapper> learningCalcAdMap,
                                     Map<Integer, AdDTOWrapper> generalCalcAdMap,
                                     AdDTOWrapper w, Integer bidStrategy, String bidAlgorithm) {
        Integer adId = w.getAdDTO().getAd().getId();
        if (useLearningBidPrice(bidStrategy, bidAlgorithm)) {
            w.setBidAlgorithmEnum(BidAlgorithmEnum.LEARNING);
            learningCalcAdMap.put(adId, w);
        } else {
            generalCalcAdMap.put(adId, w);
        }
    }

    private void handleGeneralCalcAd(Params params, BidRequest bidRequest, Imp imp, Affiliate affiliate,
                                     String taskId, Map<Integer, AdDTOWrapper> generalCalcAdMap) {
        threadPool.execute(() -> {
            try {
                String autoExploreKey = makeKey(bidRequest, imp);
                generalCalcAdMap.values().forEach(e ->
                        doCalcPriceByGeneral(e, autoExploreKey, bidRequest, imp, affiliate));
                messageQueue.putMessage(EventType.CALC_CPC_FINISH, params);
            } catch (Exception e) {
                log.error("taskId: {},calculate price by general cause a exception", taskId, e);
                messageQueue.putMessage(EventType.CALC_CPC_ERROR, params);
            }
        });
    }

    private void handleLearningCalcAd(Params params, BidRequest bidRequest, Affiliate affiliate,
                                      String taskId, Map<Integer, AdDTOWrapper> learningCalcAdMap) {
        threadPool.execute(() -> {
            try {
                doCalcPriceByLearningBid(learningCalcAdMap, bidRequest, affiliate);
                messageQueue.putMessage(EventType.CALC_CPC_FINISH, params);
            } catch (Exception e) {
                log.error("taskId: {},calculate price by learning cause a exception", taskId, e);
                messageQueue.putMessage(EventType.CALC_CPC_ERROR, params);
            }
        });
    }

    private void doCalcPriceByLearningBid(Map<Integer, AdDTOWrapper> adMap,
                                          BidRequest bidRequest, Affiliate affiliate) {
        if (adMap.isEmpty()) {
            return;
        }
        BidPriceRequest request = buildBidPriceRequest(adMap.values(), bidRequest, affiliate);
        R<List<BidPriceResponse>> r = callLearningBid(request);
        if (r.succeed()) {
            List<BidPriceResponse> responses = r.getData();
            responses.forEach(resp -> {
                BigDecimal finalPrice = BigDecimal.valueOf(resp.getBidPrice());
                finalPrice = maxPriceLimit(finalPrice);
                finalPrice = convertToUscByVivo(finalPrice, affiliate);
                adMap.get(resp.getAdId()).setBidPrice(finalPrice);
            });
        } else {
            String errorMsg = "call learning bid api error";
            log.error(errorMsg + ", code:{}, message: {}", r.getCode(), r.getMessage());
            throw new RuntimeException(errorMsg);
        }
    }

    public static BigDecimal convertToUscByVivo(BigDecimal finalPrice, Affiliate affiliate) {
        if (affiliate.getApi().equals(ProtoTransformFactory.VIVO)) {
            finalPrice = UnitConvertUtil.usdToUsc(finalPrice);
            finalPrice = finalPrice.setScale(0, RoundingMode.UP);
        }
        return finalPrice;
    }

    public static BigDecimal convertToUsdByVivo(BigDecimal finalPrice, Affiliate affiliate) {
        if (affiliate.getApi().equals(ProtoTransformFactory.VIVO)) {
            finalPrice = UnitConvertUtil.uscToUsd(finalPrice);
        }
        return finalPrice;
    }

    private R<List<BidPriceResponse>> callLearningBid(BidPriceRequest request) {
        HttpResult result = OkHttps.sync(learningBidUrl)
                .bodyType(OkHttps.JSON)
                .setBodyPara(JsonHelper.toJSONString(request))
                .post();
        return result.isSuccessful() ?
                result.getBody().toBean(new TypeRef<R<List<BidPriceResponse>>>() {
                }) : R.failure();
    }

    private BidPriceRequest buildBidPriceRequest(Collection<AdDTOWrapper> wrappers,
                                                 BidRequest bidRequest, Affiliate affiliate) {
        List<BidPriceInfo> bidPriceInfos = wrappers.stream()
                .map(w -> BidPriceInfo.builder()
                        .pctr(w.getPCtr())
                        .adId(w.getAdDTO().getAd().getId())
                        .adGroupId(w.getAdDTO().getAdGroup().getId())
                        .build())
                .collect(Collectors.toList());

        return BidPriceRequest.builder()
                .country(bidRequest.getDevice().getGeo().getCountry())
                .affiliateId(affiliate.getId())
                .bidPriceInfos(bidPriceInfos)
                .build();
    }

    private boolean useLearningBidPrice(Integer bidStrategy, String bidAlgorithm) {
        return BidStrategyEnum.CPC.getType() == bidStrategy
                && BidAlgorithmEnum.LEARNING.getType().equals(bidAlgorithm)
                && learningBidEnable;
    }

    // country_bundle_adFormat_width_height
    private String makeKey(BidRequest bidRequest, Imp imp) {
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

    private void doCalcPriceByGeneral(AdDTOWrapper adDTOWrapper, String key,
                                      BidRequest bidRequest, Imp imp, Affiliate affiliate) {
        BundleData bundleData = bundleDataManager.getBundleData(key);
        // 该广告位的曝光量小于指定大小、且开启了Bundle自动化探索开关，则进行探索
        BigDecimal finalPrice = !bundleDataManager.isImpGtSize(key) && bundleData != null
                && adDTOWrapper.getBundleTestEnable()
                ? bundleAutoExplore(bundleData, affiliate, imp, adDTOWrapper)
                : calcPriceByFormula(adDTOWrapper, bidRequest, imp);
        finalPrice = maxPriceLimit(finalPrice);
        finalPrice = convertToUscByVivo(finalPrice, affiliate);
        adDTOWrapper.setBidPrice(finalPrice);
    }

    private BigDecimal calcPriceByFormula(AdDTOWrapper adDTOWrapper, BidRequest bidRequest, Imp imp) {
        BigDecimal optPrice = BigDecimal.valueOf(adDTOWrapper.getOptPrice());
        BidStrategyEnum bidStrategy = BidStrategyEnum.of(adDTOWrapper.getBidStrategyEnum().getType());
        boolean ecpxEnable = useEcpxBidPrice(adDTOWrapper.getBidAlgorithmEnum().getType());
        BigDecimal finalPrice;
        BigDecimal eCPX = bidPriceService.getECPX(bidStrategy, bidRequest, imp);
        // 底价
        Float bidfloor = adDTOWrapper.getBidfloor();

        switch (bidStrategy) {
            case CPC:
                optPrice = getEcpxIfNotNull(adDTOWrapper, optPrice, ecpxEnable, eCPX);
                finalPrice = optPrice
                        .multiply(BigDecimal.valueOf(adDTOWrapper.getPCtr()))
                        .multiply(BigDecimal.valueOf(1000));
                break;
            case CPA:
                optPrice = getEcpxIfNotNull(adDTOWrapper, optPrice, ecpxEnable, eCPX);
                finalPrice = optPrice
                        .multiply(BigDecimal.valueOf(adDTOWrapper.getPCvr()))
                        .multiply(BigDecimal.valueOf(1000));
                break;
            case DYNAMIC:
                ThreadLocalRandom random = ThreadLocalRandom.current();
                if (adDTOWrapper.getBidProbability() > random.nextDouble(100)) {
                    finalPrice = BigDecimal.valueOf(adDTOWrapper.getBidMultiplier())
                            .multiply(BigDecimal.valueOf(bidfloor))
                            .min(optPrice);  // MAX CPM
                } else {
                    finalPrice = BigDecimal.ZERO;
                }
                break;
            case CPA_EVENT1:
            case CPA_EVENT2:
            case CPA_EVENT3:
            case CPA_EVENT10:
            case CPS:
                optPrice = getEcpxIfNotNull(adDTOWrapper, optPrice, ecpxEnable, eCPX);
                finalPrice = optPrice
                        .multiply(BigDecimal.valueOf(adDTOWrapper.getPCtr()))
                        .multiply(BigDecimal.valueOf(adDTOWrapper.getPCvr()))
                        .multiply(BigDecimal.valueOf(1000));
                break;
            case CPM:
            default:
                finalPrice = optPrice;
        }
        return finalPrice;
    }

    private static BigDecimal getEcpxIfNotNull(AdDTOWrapper wrapper, BigDecimal optPrice,
                                               boolean ecpxEnable, BigDecimal eCPX) {
        if (ecpxEnable && eCPX != null) {
            wrapper.setBidAlgorithmEnum(BidAlgorithmEnum.HISTORY_ECPX);
            return eCPX;
        }
        return optPrice;
    }

    private boolean useEcpxBidPrice(String bidAlgorithm) {
        return eCPXBidEnable && BidAlgorithmEnum.HISTORY_ECPX.getType().equals(bidAlgorithm);
    }

    private BigDecimal maxPriceLimit(BigDecimal finalPrice) {
        if (finalPrice.compareTo(this.maxPrice) > 0) {
            finalPrice = this.maxPrice;
        }
        return finalPrice;
    }

    private BigDecimal bundleAutoExplore(BundleData bundleData, Affiliate affiliate, Imp imp, AdDTOWrapper adDTOWrapper) {
        BigDecimal finalPrice;
        Double winRate = bundleData.getWinRate();
        Double bidPrice = bundleData.getBidPrice();
        if (winRate < affiliate.getRequireWinRate()) {
            // max(当前底价, ecpm) * 倍率 进行出价
            if (bidPrice < adDTOWrapper.getBidfloor()) {
                finalPrice = BigDecimal.valueOf(adDTOWrapper.getBidfloor()).multiply(multiplier);
            } else {
                finalPrice = BigDecimal.valueOf(bidPrice).multiply(multiplier);
            }
        } else {
            if (bundleData.getOldK() == 0 || bundleData.getK() == 0) {
                finalPrice = BigDecimal.valueOf(bidPrice).multiply(multiplier);
            } else {
                // k = winRate / bidPrice。 出价 = 新k的ecpm * 新k/旧k
                finalPrice = BigDecimal.valueOf(bidPrice)
                        .multiply(BigDecimal.valueOf(bundleData.getK()))
                        .divide(BigDecimal.valueOf(bundleData.getOldK()),
                                RoundingMode.HALF_UP);
            }
        }
        return finalPrice;
    }
}
