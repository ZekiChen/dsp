package com.tecdo.fsm.task.state;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.fsm.task.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitForSortState implements ITaskState {

  private final WaitForRecycleState waiForRecycleState;

  @Override
  public void handleEvent(EventType eventType, Params params, Task task) {
    switch (eventType) {
      case SORT_AD_RESPONSE:
        task.tick("task-response");
        task.cancelTimer(EventType.WAIT_SORT_AD_TIMEOUT);
        task.saveSortAdResponse(params);
        task.switchState(waiForRecycleState);
        break;
      case WAIT_SORT_AD_TIMEOUT:
        task.notifyFailed();
        task.switchState(waiForRecycleState);
        break;
      default:
        log.error("can't handel event:{}", eventType);
    }
  }
}
