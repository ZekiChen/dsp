package com.tecdo.fsm.task.state;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.fsm.task.Task;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 该状态等待回收，不处理事件
 * <p>
 * Created by Zeki on 2023/1/4
 **/
@Slf4j
@Component("TaskWaitForRecycleState")
@RequiredArgsConstructor
public class WaitForRecycleState implements ITaskState {

  @Override
  public void handleEvent(EventType eventType, Params params, Task task) {
    log.error("Task can't handle event, type: {}", eventType);
  }
}
