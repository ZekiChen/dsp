package com.tecdo.fsm.task.state;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.controller.MessageQueue;
import com.tecdo.fsm.task.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 该状态内会进行价格计算
 *
 * Created by Zeki on 2023/1/4
 **/
@Slf4j
@Component
public class WaitForPredictState implements ITaskState {

    @Autowired
    private MessageQueue messageQueue;
    @Autowired
    private WaitForCalcPriceState waitForCalcPriceState;

    @Override
    public void handleEvent(EventType eventType, Params params, Task task) {
        switch (eventType) {
            case RECALL_FINISH:
                // TODO
                task.switchState(waitForCalcPriceState);
                break;
            default:
                log.error("Task can't handle event, type: {}", eventType);
        }
    }
}
