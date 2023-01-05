package com.tecdo.fsm.task.state;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.controller.MessageQueue;
import com.tecdo.fsm.task.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 该状态内会进行 CTR 预估
 *
 * Created by Zeki on 2023/1/4
 **/
@Slf4j
@Component
public class WaitForRecallState implements ITaskState {

    @Autowired
    private MessageQueue messageQueue;
    @Autowired
    private WaitForPredictState waitForPredictState;

    @Override
    public void handleEvent(EventType eventType, Params params, Task task) {
        switch (eventType) {
            case RECALL_FINISH:
                // TODO
                task.switchState(waitForPredictState);
                break;
            default:
                log.error("Task can't handle event, type: {}", eventType);
        }
    }
}
