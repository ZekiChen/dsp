package com.tecdo.fsm.task.state;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.fsm.task.Task;

public interface ITaskState {

  void handleEvent(EventType eventType, Params params, Task task);

}
