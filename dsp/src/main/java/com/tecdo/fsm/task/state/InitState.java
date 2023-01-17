package com.tecdo.fsm.task.state;

import com.tecdo.common.Params;
import com.tecdo.common.ThreadPool;
import com.tecdo.constant.Constant;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.fsm.task.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 该状态内会进行广告召回
 *
 * Created by Zeki on 2023/1/4
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class InitState implements ITaskState {

    private final MessageQueue messageQueue;
    private final WaitForRecallState waitForRecallState;

    @Override
    public void handleEvent(EventType eventType, Params params, Task task) {
        switch (eventType) {
            case TASK_START:
                ThreadPool.getInstance().execute(() -> {
                    try {
                        params.put(ParamKey.ADS_IMP_KEY, task.listRecallAd());
                        messageQueue.putMessage(EventType.ADS_RECALL_FINISH);
                    } catch (Exception e) {
                        log.error("list recall ad error, imp id: {}, so this request will not participate in bidding, reason: {}",
                                task.getImp().getId(), e.getMessage());
                        messageQueue.putMessage(EventType.ADS_RECALL_ERROR);
                    }
                });
                task.startTimer(EventType.ADS_RECALL_TIMEOUT, params, Constant.TIMEOUT_ADS_RECALL);
                task.switchState(waitForRecallState);
                break;
            default:
                log.error("Task can't handle event, type: {}", eventType);
        }
    }
}
