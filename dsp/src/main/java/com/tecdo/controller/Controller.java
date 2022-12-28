package com.tecdo.controller;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.service.ContextManager;
import com.tecdo.service.LifeCycleManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by Zeki on 2022/12/27
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class Controller implements MessageObserver {

    private final LifeCycleManager lifeCycleManager;
    private final ContextManager contextManager;

    @Override
    public void handle(EventType eventType, Params params) {
        switch (eventType) {
            case SERVER_START:
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
