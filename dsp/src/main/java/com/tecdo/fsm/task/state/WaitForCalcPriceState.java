package com.tecdo.fsm.task.state;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.controller.MessageQueue;
import com.tecdo.fsm.task.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Zeki on 2023/1/4
 **/
@Slf4j
@Component
public class WaitForCalcPriceState implements ITaskState {

    @Autowired
    private MessageQueue messageQueue;

    @Override
    public void handleEvent(EventType eventType, Params params, Task task) {
        switch (eventType) {
            case ADS_RECALL_FINISH:
                // TODO
                break;
            default:
                log.error("Task can't handle event, type: {}", eventType);
        }
    }
}
