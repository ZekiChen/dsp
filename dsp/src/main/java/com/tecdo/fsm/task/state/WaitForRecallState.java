package com.tecdo.fsm.task.state;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.zhxu.okhttps.HttpResult;
import cn.zhxu.okhttps.OkHttps;
import com.tecdo.common.Params;
import com.tecdo.common.ThreadPool;
import com.tecdo.constant.Constant;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.domain.biz.base.R;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.biz.request.CtrRequest;
import com.tecdo.domain.biz.response.CtrResponse;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.Ad;
import com.tecdo.entity.Affiliate;
import com.tecdo.enums.biz.AdTypeEnum;
import com.tecdo.fsm.task.Task;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 该状态内会进行 CTR 预估
 *
 * Created by Zeki on 2023/1/4
 **/
@Slf4j
@Component
public class WaitForRecallState implements ITaskState {

    @Autowired
    private MessageQueue messageQueue;
    @Autowired
    private WaitForCtrPredictState waitForCtrPredictState;

    @Value("${pac.ctr-predict.url}")
    private String ctrPredictUrl;

    @Override
    public void handleEvent(EventType eventType, Params params, Task task) {
        switch (eventType) {
            case ADS_RECALL_FINISH:
                task.cancelTimer();
                Params taskParams = null;
                // TODO 根据 http request 中的 token 获取 affId
                Integer affId = getAffIdByReqToken(null, taskParams.get(ParamKey.AFFILIATES_CACHE_KEY));
                ThreadPool.getInstance().execute(() -> {
                    List<CtrRequest> ctrRequests = taskParams.<Map<Integer, AdDTO>>get(ParamKey.ADS_RECALL_KEY).values().stream().map(adDTO ->
                            buildCtrRequest(task.getBidRequest(), task.getImp(), affId, adDTO)).collect(Collectors.toList());
                    Map<String, Object> paramMap = MapUtil.<String, Object>builder().put("data", ctrRequests).build();
                    HttpResult httpResult = OkHttps.sync(ctrPredictUrl).addBodyPara(paramMap).get();
                    if (httpResult.isSuccessful()) {
                        R<List<CtrResponse>> result = httpResult.getBody().toBean(R.class);
                        taskParams.put(ParamKey.CTR_PREDICT_KEY, result.getData());
                        messageQueue.putMessage(EventType.CTR_PREDICT_FINISH, taskParams);
                    } else {
                        log.error("ctr request error: {}, reason: {}", httpResult.getStatus(), httpResult.getError().getMessage());
                        messageQueue.putMessage(EventType.CTR_PREDICT_ERROR);
                    }
                });
                task.startTimer(Constant.TIMEOUT_PRE_DICT);
                task.switchState(waitForCtrPredictState);
                break;
            case ADS_RECALL_ERROR:
                task.cancelTimer();
                // 本次 bid 不参与
                break;
            case ADS_RECALL_TIMEOUT:
                // TODO 重试广告召回？次数？还是本次bid不参与？
                break;
            default:
                log.error("Task can't handle event, type: {}", eventType);
        }
    }

    @Nullable
    private static Integer getAffIdByReqToken(String token, Map<Integer, Affiliate> affiliateMap) {
        Affiliate affiliate = affiliateMap.values().stream().filter(e -> token.equals(e.getSecret())).findFirst().orElse(null);
        return affiliate != null ? affiliate.getId() : null;
    }

    private CtrRequest buildCtrRequest(BidRequest bidRequest, Imp imp, Integer affId, AdDTO adDTO) {
        return CtrRequest.builder()
                .adId(adDTO.getAd().getId())
                .day(DateUtil.today())
                .affiliateId(affId)
                .adType(adDTO.getAd().getType().toString())
                .adHeight(adDTO.getCreativeMap().get(getCreativeIdByAd(adDTO.getAd())).getHeight())
                .adWidth(adDTO.getCreativeMap().get(getCreativeIdByAd(adDTO.getAd())).getWidth())
                .os(bidRequest.getDevice().getOs())
                .deviceMake(bidRequest.getDevice().getMake())
                .bundle(bidRequest.getApp().getBundle())
                .country(bidRequest.getDevice().getGeo() != null ? bidRequest.getDevice().getGeo().getCountry() : null)
                .creativeId(getCreativeIdByAd(adDTO.getAd()))
                .bidFloor(Double.valueOf(imp.getBidfloor()))
                .rtaFeature(adDTO.getCampaignRtaInfo() != null ? adDTO.getCampaignRtaInfo().getRtaFeature() : null)
                .packageName(adDTO.getCampaign().getPackageName())
                .category(adDTO.getCampaign().getCategory())
                .build();
    }

    private Integer getCreativeIdByAd(Ad ad) {
        switch (AdTypeEnum.of(ad.getType())) {
            case BANNER:
                return ad.getIcon();
            case VIDEO:
                return ad.getVideo();
            case NATIVE:
                return ad.getImage();
        }
        return null;
    }

}
