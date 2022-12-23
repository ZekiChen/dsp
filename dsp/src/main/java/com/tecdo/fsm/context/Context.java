package com.tecdo.fsm.context;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.server.request.HttpRequest;

public class Context {

  private IContextState currentState;

  public void handleEvent(EventType eventType, Params params) {
    currentState.handleEvent(eventType, params, this);
  }

  public void init(HttpRequest httpRequest) {

  }

  public void reset() {

  }

  public void switchState(IContextState newState) {
    this.currentState = newState;
  }

}
