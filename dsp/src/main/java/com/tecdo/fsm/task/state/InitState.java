package com.tecdo.fsm.task.state;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.controller.MessageQueue;
import com.tecdo.domain.biz.dto.AdDTO;
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
                try {
                    Map<Integer, AdDTO> adDTOMap = task.listRecallAd();
                } catch (Exception e) {
                    log.error("Task error, so this request will not participate in bidding: {}", e.getMessage());
                    messageQueue.putMessage(EventType.RECALL_ERROR);
                    break;
                }
                // TODO
                messageQueue.putMessage(EventType.RECALL_FINISH);
                task.switchState(waitForRecallState);
                break;
            default:
                log.error("Task can't handle event, type: {}", eventType);
        }
    }
}
