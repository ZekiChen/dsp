package com.tecdo.fsm.task.state;

import com.tecdo.common.Params;
import com.tecdo.constant.Constant;
import com.tecdo.constant.EventType;
import com.tecdo.fsm.task.Task;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 该状态内会进行广告召回
 * <p>
 * Created by Zeki on 2023/1/4
 **/
@Slf4j
@Component("TaskInitState")
@RequiredArgsConstructor
public class InitState implements ITaskState {


  private final WaitForRecallState waitForRecallState;

  @Override
  public void handleEvent(EventType eventType, Params params, Task task) {
    switch (eventType) {
      case TASK_START:
        task.listRecallAd();
        task.startTimer(EventType.ADS_RECALL_TIMEOUT,
                        task.assignParams(),
                        Constant.TIMEOUT_ADS_RECALL);
        task.switchState(waitForRecallState);
        break;
      default:
        log.error("Task can't handle event, type: {}", eventType);
    }
  }
}
