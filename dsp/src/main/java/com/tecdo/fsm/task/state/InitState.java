package com.tecdo.fsm.task.state;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.fsm.task.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

  @Value("${pac.timeout.task.ad.recall}")
  private long timeoutRecall;

  @Value("${pac.task.ad.recall.batch-enable:false}")
  private boolean recallBatchEnable;

  @Override
  public void handleEvent(EventType eventType, Params params, Task task) {
    switch (eventType) {
      case TASK_START:
        task.tick("task-ad-recall");
        task.listRecallAd(recallBatchEnable);
        task.startTimer(EventType.ADS_RECALL_TIMEOUT,
                        task.assignParams(),
                        timeoutRecall);
        task.switchState(waitForRecallState);
        break;
      default:
        log.error("Task can't handle event, type: {}", eventType);
    }
  }
}
