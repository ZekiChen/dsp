package com.tecdo.fsm.context.state;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;

import com.tecdo.fsm.context.Context;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("ContextWaitForRecycleState")
@RequiredArgsConstructor
public class WaitForRecycleState implements IContextState {

  @Override
  public void handleEvent(EventType eventType, Params params, Context context) {
    log.error("can't handel event:{}", eventType);
  }
}
