package com.tecdo.service;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.service.init.AdManager;
import com.tecdo.service.init.AffiliateManager;
import com.tecdo.service.init.ConditionManager;
import com.tecdo.service.init.RtaManager;
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

    private final AffiliateManager affiliateManager;
    private final AdManager adManager;
    private final ConditionManager conditionManager;
    private final RtaManager rtaManager;

    private State currentState = State.INIT;

    @AllArgsConstructor
    private enum State {
        INIT(1, "init"),
        WAIT_DATA_INIT_COMPLETED(2, "waiting data init completed");

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
            case AFFILIATES_LOAD_SUCCESS:
            case AFFILIATES_LOAD_TIMEOUT:
                affiliateManager.handleEvent(eventType, params);
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
                affiliateManager.init();
                // TODO
                adManager.init();
                conditionManager.init();
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
                log.info("DB data init finish!");
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }
}
