package com.tecdo.fsm.context;

import com.tecdo.common.Instance;
import com.tecdo.common.Params;
import com.tecdo.constant.Constant;
import com.tecdo.constant.EventType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InitState implements IContextState {

  @Override
  public void handleEvent(EventType eventType, Params params, Context context) {
    switch (eventType) {
      case RECEIVE_BID_REQUEST:
        context.startTimer(EventType.WAIT_TASK_RESPONSE_TIMEOUT,
                           context.assignParams(),
                           Constant.TIMEOUT_WAIT_TASK_RESPONSE);
        context.handleBidRequest();
        context.switchState(Instance.of(WaitForAllResponseState.class));

        break;
      default:
        log.error("can't handel event:{}", eventType);
    }
  }
}
