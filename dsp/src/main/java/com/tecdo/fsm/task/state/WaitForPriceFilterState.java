package com.tecdo.fsm.task.state;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.fsm.task.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by Zeki on 2023/9/19
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WaitForPriceFilterState implements ITaskState {

    private final WaitForRtaState waitForRtaState;
    private final WaitForRecycleState waitForRecycleState;

    @Value("${pac.timeout.task.rta.response}")
    private long timeoutRtaResponse;

    @Override
    public void handleEvent(EventType eventType, Params params, Task task) {
        switch (eventType) {
            case PRICE_FILTER_FINISH:
                task.tick("task-rta-request");
                task.cancelTimer(EventType.PRICE_FILTER_TIMEOUT);
                Map<Integer, AdDTOWrapper> afterPriceFilterAdMap = task.savePriceFilterResponse(params);
                if (afterPriceFilterAdMap.isEmpty()) {
                    task.impNotBid();
                    task.switchState(waitForRecycleState);
                    return;
                }
                task.requestRta();
                task.startTimer(EventType.WAIT_REQUEST_RTA_RESPONSE_TIMEOUT, params, timeoutRtaResponse);
                task.switchState(waitForRtaState);
                break;
            case PRICE_FILTER_TIMEOUT:
                task.notifyFailed(eventType);
                task.switchState(waitForRecycleState);
                break;
            default:
                log.error("Task can't handle event, type: {}", eventType);
        }
    }
}
