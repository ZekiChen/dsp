package com.tecdo.fsm.context;

import cn.hutool.extra.spring.SpringUtil;
import com.tecdo.common.Params;
import com.tecdo.common.ThreadPool;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.RtaInfo;
import com.tecdo.fsm.task.Task;
import com.tecdo.fsm.task.TaskPool;
import com.tecdo.server.request.HttpRequest;
import com.tecdo.service.init.RtaInfoManager;
import com.tecdo.service.rta.RtaHelper;
import com.tecdo.service.rta.Target;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    private final TaskPool taskPool = SpringUtil.getBean(TaskPool.class);

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
            Task task = taskPool.get();
            task.init(bidRequest, imp);
            taskMap.put(imp.getId(), task);
            messageQueue.putMessage(EventType.RECEIVE_BID_REQUEST, Params.create());
        });
    }

    public void saveTaskResponse(Params params) {

    }

    public boolean isReceiveAllTaskResponse() {
        return taskResponse.size() == taskMap.size();
    }

    public void requestRta() {
        Params params = assignParams();
        ThreadPool.getInstance().execute(() -> {

            try {
                Map<Integer, Target> rtaResMap = doRequestRta();
                messageQueue.putMessage(EventType.REQUEST_RTA_RESPONSE,
                        params.put(ParamKey.REQUEST_RTA_RESPONSE, rtaResMap));
            } catch (Exception e) {
                messageQueue.putMessage(EventType.WAIT_REQUEST_RTA_RESPONSE_ERROR, params);
            }
        });
    }

    public Map<Integer, Target> doRequestRta() {
        List<AdDTO> adDTOList = null;
        // todo 协议中的是国家三字码，需要转为对应的二字码
        String country = bidRequest.getDevice().getGeo().getCountry();
        String deviceId = bidRequest.getDevice().getIfa();
        Map<Integer, Target> rtaResMap = new HashMap<>();

        // 只保留rta的单子，并将单子按照广告主分组
        Map<Integer, List<AdDTO>> advToAdList = adDTOList.stream()
                .filter(i -> Objects.nonNull(i.getCampaignRtaInfo()))
                .collect(Collectors.groupingBy(adDTO -> adDTO.getCampaignRtaInfo()
                        .getAdvId()));
        // 分广告主进行rta匹配
        advToAdList.forEach((advId, adList) -> {
            RtaInfo rtaInfo = rtaInfoManager.getRtaInfo(advId);
            RtaHelper.requestRta(rtaInfo, adList, country, deviceId, rtaResMap);
        });
        return rtaResMap;
    }

    public void saveRtaResponse(Params params) {
        List<AdDTO> adDTOList = null;
        Map<Integer, Target> rtaResMap = params.get(ParamKey.REQUEST_RTA_RESPONSE);
        Map<Integer, List<AdDTO>> campaignIdToAdList =
                adDTOList.stream().collect(Collectors.groupingBy(adDTO -> adDTO.getCampaign().getId()));
        // 将rta匹配的结果保存到AdDTO中
        for (Map.Entry<Integer, Target> entry : rtaResMap.entrySet()) {
            Integer campaignId = entry.getKey();
            Target t = entry.getValue();
            if (t.isTarget()) {
                String token = t.getToken();
                campaignIdToAdList.get(campaignId).forEach(i -> i.setRtaToken(token));
            }
        }
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
