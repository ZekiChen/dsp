package com.tecdo.job.service.init;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.common.thread.ThreadPool;
import com.tecdo.common.util.Params;
import com.tecdo.job.constant.EventType;
import com.tecdo.job.controller.MessageQueue;
import com.tecdo.job.controller.SoftTimer;
import com.tecdo.job.entity.Campaign;
import com.tecdo.job.mapper.CampaignMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.tecdo.job.constant.ParamKey;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2022/12/27
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignManager extends ServiceImpl<CampaignMapper, Campaign> {

    private final SoftTimer softTimer;
    private final MessageQueue messageQueue;
    private final ThreadPool threadPool;

    private State currentState = State.INIT;
    private long timerId;

    private Map<Integer, Campaign> campaignMap;

    @Value("${pac.timeout.load.db.default}")
    private long loadTimeout;
    @Value("${pac.interval.reload.db.default}")
    private long reloadInterval;

    /**
     * 从 DB 加载 campaign 集合，每 5 分钟刷新一次缓存
     */
    public Map<Integer, Campaign> getCampaignMap() {
        return this.campaignMap;
    }

    /**
     * DSP预算总和
     */
    public Double dailyBudget() {
        return campaignMap.values().stream().map(Campaign::getDailyBudget).reduce(Double::sum).orElse(0d);
    }

    public Campaign getCampaign(String id){
        return campaignMap.get(id);
    }

    @AllArgsConstructor
    private enum State {
        INIT(1, "init"),
        WAIT_INIT_RESPONSE(2, "waiting init response"),
        RUNNING(3,"init success, now is running"),
        UPDATING(4,"updating");

        private int code;
        private String desc;

        @Override
        public String toString() {
            return code + " - " + desc;
        }
    }

    public void init(Params params) {
        messageQueue.putMessage(EventType.CAMPAIGNS_LOAD, params);
    }

    private void startReloadTimeoutTimer(Params params) {
        timerId = softTimer.startTimer(EventType.CAMPAIGNS_LOAD_TIMEOUT, params, loadTimeout);
    }

    private void cancelReloadTimeoutTimer() {
        softTimer.cancel(timerId);
    }

    private void startNextReloadTimer(Params params) {
        softTimer.startTimer(EventType.CAMPAIGNS_LOAD, params, reloadInterval);
    }

    public void switchState(State state) {
        this.currentState = state;
    }

    public void handleEvent(EventType eventType, Params params) {
        switch (eventType) {
            case CAMPAIGNS_LOAD:
                handleCampaignsReload(params);
                break;
            case CAMPAIGNS_LOAD_RESPONSE:
                handleCampaignsResponse(params);
                break;
            case CAMPAIGNS_LOAD_ERROR:
                handleCampaignsError(params);
                break;
            case CAMPAIGNS_LOAD_TIMEOUT:
                handleCampaignsTimeout(params);
                break;
            default:
                log.error("Can't handle event, type: {}", eventType);
        }
    }

    private void handleCampaignsReload(Params params) {
        switch (currentState) {
            case INIT:
            case RUNNING:
                threadPool.execute(() -> {
                    try {
                        Map<Integer, Campaign> campaignMap = list().stream().collect(Collectors.toMap(Campaign::getId, e -> e));
                        params.put(ParamKey.CAMPAIGNS_CACHE_KEY, campaignMap);
                        messageQueue.putMessage(EventType.CAMPAIGNS_LOAD_RESPONSE, params);
                    } catch (Exception e) {
                        log.error("affiliates load failure from db", e);
                        messageQueue.putMessage(EventType.CAMPAIGNS_LOAD_ERROR, params);
                    }
                });
                startReloadTimeoutTimer(params);
                switchState(currentState == State.INIT ? State.WAIT_INIT_RESPONSE : State.UPDATING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleCampaignsResponse(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
                messageQueue.putMessage(EventType.ONE_DATA_READY);
            case UPDATING:
                cancelReloadTimeoutTimer();
                this.campaignMap = params.get(ParamKey.CAMPAIGNS_CACHE_KEY);
                log.info("affiliates load success, size: {}", campaignMap.size());
                startNextReloadTimer(params);
                switchState(State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleCampaignsError(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
            case UPDATING:
                cancelReloadTimeoutTimer();
                startNextReloadTimer(params);
                switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleCampaignsTimeout(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
            case UPDATING:
                startNextReloadTimer(params);
                switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

}
