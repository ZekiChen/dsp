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

    private State currentState = State.INIT_STATE;

    @AllArgsConstructor
    private enum State {
        INIT_STATE(1, "init state"),
        WAITING_DB_DATA_INIT_COMPLETED(2, "waiting db data init completed");

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
                loadDbDataInit();
                break;
            case DB_DATA_INIT_COMPLETE:
                finishDbDataInit();
                break;
            default:
                log.error("Can't handle event, type: {}", eventType);
                break;
        }
    }

    private void loadDbDataInit() {
        switch (currentState) {
            case INIT_STATE:
                affiliateManager.init();
                adManager.init();
                conditionManager.init();
                rtaManager.init();
                switchState(State.WAITING_DB_DATA_INIT_COMPLETED);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
                break;
        }
    }

    private void finishDbDataInit() {
        switch (currentState) {
            case WAITING_DB_DATA_INIT_COMPLETED:
                log.info("DB data init finish!");
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
                break;
        }
    }
}
