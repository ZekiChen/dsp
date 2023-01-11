package com.tecdo.fsm.task.state;

import com.tecdo.common.Params;
import com.tecdo.constant.Constant;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.fsm.task.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
                    // TODO 需要有一个 Task 关联的 Params 对象，task-imp-taskParams，其他 State 也持有引用
                    Params taskParams = null;
                    taskParams.put(ParamKey.ADS_RECALL_KEY, task.listRecallAd());
                    messageQueue.putMessage(EventType.ADS_RECALL_FINISH);
                } catch (Exception e) {
                    log.error("Task error, so this request will not participate in bidding: {}", e.getMessage());
                    messageQueue.putMessage(EventType.ADS_RECALL_ERROR);
                    break;
                }
                task.startTimer(Constant.TIMEOUT_ADS_RECALL);
                task.switchState(waitForRecallState);
                break;
            default:
                log.error("Task can't handle event, type: {}", eventType);
        }
    }
}
