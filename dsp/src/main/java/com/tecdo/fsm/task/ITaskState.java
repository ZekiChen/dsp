package com.tecdo.fsm.task;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;

public interface ITaskState {

  void handleEvent(EventType eventType, Params params, Task task);

}
