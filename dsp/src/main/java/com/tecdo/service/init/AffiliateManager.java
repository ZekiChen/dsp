package com.tecdo.service.init;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.common.Params;
import com.tecdo.common.ThreadPool;
import com.tecdo.constant.Constant;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.entity.Affiliate;
import com.tecdo.entity.base.IdEntity;
import com.tecdo.mapper.AffiliateMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2022/12/27
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateManager extends ServiceImpl<AffiliateMapper, Affiliate> {

    private final SoftTimer softTimer;
    private final MessageQueue messageQueue;

    private State currentState = State.INIT;
    private long timerId;

    private Map<String, Affiliate> affiliateMap;

    /**
     * 从 DB 加载 affiliate 集合，每 5 分钟刷新一次缓存
     */
    public Map<String, Affiliate> getAffiliateMap() {
        return this.affiliateMap;
    }

    public Affiliate getAffiliate(String secret){
        return affiliateMap.get(secret);
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

    public void init() {
        messageQueue.putMessage(EventType.AFFILIATES_LOAD);
    }

    private void startReloadTimeoutTimer() {
        timerId = softTimer.startTimer(EventType.AFFILIATES_LOAD_TIMEOUT, null, Constant.TIMEOUT_LOAD_DB_CACHE_GENERAL);
    }

    private void cancelReloadTimeoutTimer() {
        softTimer.cancel(timerId);
    }

    private void startNextReloadTimer() {
        softTimer.startTimer(EventType.AFFILIATES_LOAD, null, Constant.INTERVAL_RELOAD_DB_CACHE);
    }

    public void switchState(State state) {
        this.currentState = state;
    }

    public void handleEvent(EventType eventType, Params params) {
        switch (eventType) {
            case AFFILIATES_LOAD:
                handleAffiliatesReload();
                break;
            case AFFILIATES_LOAD_RESPONSE:
                handleAffiliatesResponse(params);
                break;
            case AFFILIATES_LOAD_ERROR:
                handleAffiliatesError();
                break;
            case AFFILIATES_LOAD_TIMEOUT:
                handleAffiliatesTimeout();
                break;
            default:
                log.error("Can't handle event, type: {}", eventType);
        }
    }

    private void handleAffiliatesReload() {
        switch (currentState) {
            case INIT:
            case RUNNING:
                ThreadPool.getInstance().execute(() -> {
                    try {
                        Map<String, Affiliate> affiliateMap = list().stream().collect(Collectors.toMap(Affiliate::getSecret, e -> e));
                        Params params = Params.create(ParamKey.AFFILIATES_CACHE_KEY, affiliateMap);
                        messageQueue.putMessage(EventType.AFFILIATES_LOAD_RESPONSE, params);
                    } catch (Exception e) {
                        log.error("affiliates load failure from db: {}", e.getMessage());
                        messageQueue.putMessage(EventType.AFFILIATES_LOAD_ERROR);
                    }
                });
                startReloadTimeoutTimer();
                switchState(currentState == State.INIT ? State.WAIT_INIT_RESPONSE : State.UPDATING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleAffiliatesResponse(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
                messageQueue.putMessage(EventType.A_DATA_READY);
            case UPDATING:
                cancelReloadTimeoutTimer();
                this.affiliateMap = params.get(ParamKey.AFFILIATES_CACHE_KEY);
                log.info("affiliates load success, size: {}", affiliateMap.size());
                startNextReloadTimer();
                switchState(State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleAffiliatesError() {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
            case UPDATING:
                cancelReloadTimeoutTimer();
                startNextReloadTimer();
                switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleAffiliatesTimeout() {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
            case UPDATING:
                startNextReloadTimer();
                switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

}
