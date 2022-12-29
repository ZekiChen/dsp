package com.tecdo.service;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.controller.MessageQueue;
import com.tecdo.service.init.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by Zeki on 2022/12/27
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class LifeCycleManager {

    private final AffiliateManager affManager;
    private final AdManager adManager;
    private final RtaManager rtaManager;

    private final MessageQueue messageQueue;

    private State currentState = State.INIT;

    @AllArgsConstructor
    private enum State {
        INIT(1, "init"),
        WAIT_DATA_INIT_COMPLETED(2, "waiting data init completed"),
        RUNNING(3, "data init success, now is running");

        private int code;
        private String desc;

        @Override
        public String toString() {
            return code + " - " + desc;
        }
    }

    private void switchState(State state) {
        currentState = state;
    }

    public void handleEvent(EventType eventType, Params params) {
        switch (eventType) {
            case SERVER_START:
                handleDbDataInit();
                break;
            case AFFILIATES_LOAD:
            case AFFILIATES_LOAD_RESPONSE:
            case AFFILIATES_LOAD_ERROR:
            case AFFILIATES_LOAD_TIMEOUT:
                affManager.handleEvent(eventType, params);
                break;
            case ADS_LOAD:
            case ADS_LOAD_RESPONSE:
            case ADS_LOAD_ERROR:
            case ADS_LOAD_TIMEOUT:
                adManager.handleEvent(eventType, params);
                break;
            case RTA_INFOS_LOAD:
            case RTA_INFOS_LOAD_RESPONSE:
            case RTA_INFOS_LOAD_ERROR:
            case RTA_INFOS_LOAD_TIMEOUT:
                rtaManager.handleEvent(eventType, params);
                break;
            case DB_DATA_INIT_COMPLETE:
                handleFinishDbDataInit();
                break;
            default:
                log.error("Can't handle event, type: {}", eventType);
        }
    }

    private void handleDbDataInit() {
        switch (currentState) {
            case INIT:
                affManager.init();
                adManager.init();
                rtaManager.init();
                switchState(State.WAIT_DATA_INIT_COMPLETED);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleFinishDbDataInit() {
        switch (currentState) {
            case WAIT_DATA_INIT_COMPLETED:
                switchState(State.RUNNING);
                log.info("DB data init finish!");
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }
}
