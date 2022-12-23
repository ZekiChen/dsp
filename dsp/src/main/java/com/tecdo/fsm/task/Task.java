package com.tecdo.fsm.task;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;

public class Task {

  private ITaskState currentState;

  public void handleEvent(EventType eventType, Params params) {
    currentState.handleEvent(eventType, params, this);
  }

  public void init() {

  }

  public void reset() {

  }

  public void switchState(ITaskState newState) {
    this.currentState = newState;
  }

}
