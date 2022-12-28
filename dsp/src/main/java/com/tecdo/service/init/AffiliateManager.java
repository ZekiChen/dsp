package com.tecdo.service.init;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.common.Params;
import com.tecdo.common.ThreadPool;
import com.tecdo.constant.Constant;
import com.tecdo.constant.EventType;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.entity.Affiliate;
import com.tecdo.mapper.AffiliateMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeki on 2022/12/27
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateManager extends ServiceImpl<AffiliateMapper, Affiliate> {

    private final MessageQueue messageQueue;
    private final SoftTimer softTimer;

    private State currentState = State.INIT;
    private long timerId;

    private List<Affiliate> affiliates;

    /**
     * 拿到 affiliate 集合，每 5 分钟刷新一次缓存
     */
    public List<Affiliate> listAffiliate() {
        return this.affiliates;
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
        timerId = softTimer.startTimer(EventType.AFFILIATES_LOAD_TIMEOUT, null, Constant.TIMEOUT_LOAD_DB_CACHE);
    }

    private void cancelReloadTimeoutTimer() {
        softTimer.cancel(timerId);
    }

    private void startNextReloadTimer() {
        softTimer.startTimer(EventType.AFFILIATES_LOAD, null, Constant.RELOAD_DB_CACHE);
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
                handleAffiliatesResponse();
                break;
            case AFFILIATES_LOAD_SUCCESS:
                handleAffiliatesSuccess();
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
                    this.affiliates = list();
                    messageQueue.putMessage(EventType.AFFILIATES_LOAD_RESPONSE);
                });
                startReloadTimeoutTimer();
                switchState(currentState == State.INIT ? State.WAIT_INIT_RESPONSE : State.UPDATING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleAffiliatesResponse() {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
            case UPDATING:
                messageQueue.putMessage(EventType.AFFILIATES_LOAD_SUCCESS);
                cancelReloadTimeoutTimer();
                startNextReloadTimer();
                switchState(State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleAffiliatesSuccess() {
        log.info("affiliates load success, size: {}", affiliates.size());
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
