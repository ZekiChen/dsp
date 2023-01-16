package com.tecdo.fsm.task.state;

import com.tecdo.common.Params;
import com.tecdo.common.ThreadPool;
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
                ThreadPool.getInstance().execute(() -> {
                    try {
                        // TODO 需要有一个 Task 关联的 Params 对象，task-imp-taskParams，其他 State 也持有引用
                        Params taskParams = null;
                        taskParams.put(ParamKey.ADS_IMP_KEY, task.listRecallAd());
                        messageQueue.putMessage(EventType.ADS_RECALL_FINISH);
                    } catch (Exception e) {
                        log.error("list recall ad error, imp id: {}, so this request will not participate in bidding, reason: {}",
                                task.getImp().getId(), e.getMessage());
                        messageQueue.putMessage(EventType.ADS_RECALL_ERROR);
                    }
                });
                task.startTimer(Constant.TIMEOUT_ADS_RECALL);
                task.switchState(waitForRecallState);
                break;
            default:
                log.error("Task can't handle event, type: {}", eventType);
        }
    }
}
