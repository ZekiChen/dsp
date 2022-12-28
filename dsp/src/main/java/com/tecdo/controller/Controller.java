package com.tecdo.controller;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.fsm.ContextManager;
import com.tecdo.service.LifeCycleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Zeki on 2022/12/27
 **/
@Slf4j
@Component
public class Controller implements MessageObserver {

    @Autowired
    private LifeCycleManager lifeCycleManager;
    @Autowired
    private ContextManager contextManager;

    @Override
    public void handle(EventType eventType, Params params) {
        switch (eventType) {
            case SERVER_START:
            case AFFILIATES_LOAD:
            case AFFILIATES_LOAD_RESPONSE:
            case AFFILIATES_LOAD_SUCCESS:
            case AFFILIATES_LOAD_TIMEOUT:
            case DB_DATA_INIT_COMPLETE:
                lifeCycleManager.handleEvent(eventType, params);
                break;
            case CONTEXT_START:
                contextManager.handleEvent(eventType, params);
                break;
            default:
                log.error("Can't handle event: {} ", eventType);
                break;
        }
    }
}
