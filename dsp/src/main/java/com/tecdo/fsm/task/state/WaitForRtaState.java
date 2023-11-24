package com.tecdo.fsm.task.state;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.fsm.task.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitForRtaState implements ITaskState {

  private final WaitForSortState waitForSortState;
  private final WaitForRecycleState waiForRecycleState;

  @Value("${pac.timeout.task.bid-price-sort}")
  private long sortTimeout;

  @Override
  public void handleEvent(EventType eventType, Params params, Task task) {
    switch (eventType) {
      case REQUEST_RTA_RESPONSE:
        if (task.rtaResponseFinish()) {
          task.cancelTimer(EventType.WAIT_REQUEST_RTA_RESPONSE_TIMEOUT);
          task.saveRtaResponse(params);
          if (task.isImpBid()) {
            task.tick("task-sort");
            task.sort();
            task.startTimer(EventType.WAIT_SORT_AD_TIMEOUT, task.assignParams(), sortTimeout);
            task.switchState(waitForSortState);
          } else {
            task.impNotBid();
            task.switchState(waiForRecycleState);
          }
        }
        break;
      case WAIT_REQUEST_RTA_RESPONSE_TIMEOUT:
        task.notifyFailed(eventType);
        task.switchState(waiForRecycleState);
        break;
      case WAIT_REQUEST_RTA_RESPONSE_ERROR:
        task.cancelTimer(EventType.WAIT_REQUEST_RTA_RESPONSE_TIMEOUT);
        task.notifyFailed(eventType);
        task.switchState(waiForRecycleState);
        break;
      default:
        log.error("can't handel event:{}", eventType);
    }
  }
}
