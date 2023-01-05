package com.tecdo.fsm.task.state;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.controller.MessageQueue;
import com.tecdo.domain.dto.AdDTO;
import com.tecdo.fsm.task.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 该状态内会进行广告召回
 *
 * Created by Zeki on 2023/1/4
 **/
@Slf4j
@Component
public class InitState implements ITaskState {

    @Autowired
    private MessageQueue messageQueue;
    @Autowired
    private WaitForRecallState waitForRecallState;

    @Override
    public void handleEvent(EventType eventType, Params params, Task task) {
        switch (eventType) {
            case TASK_START:
                Map<Integer, AdDTO> adDTOMap = task.listRecallAd();
                // TODO
                messageQueue.putMessage(EventType.RECALL_FINISH);
                task.switchState(waitForRecallState);
                break;
            default:
                log.error("Task can't handle event, type: {}", eventType);
        }
    }
}
