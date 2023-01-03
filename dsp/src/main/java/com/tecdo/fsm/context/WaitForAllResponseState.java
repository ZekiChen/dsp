package com.tecdo.fsm.context;

import com.tecdo.common.Instance;
import com.tecdo.common.Params;
import com.tecdo.constant.Constant;
import com.tecdo.constant.EventType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WaitForAllResponseState implements IContextState {
  @Override
  public void handleEvent(EventType eventType, Params params, Context context) {
    switch (eventType) {
      case RECEIVE_TASK_RESPONSE:
        context.saveTaskResponse(params);
        boolean receiveAllTaskResponse = context.isReceiveAllTaskResponse();
        if (receiveAllTaskResponse) {
          context.cancelTimer(EventType.WAIT_TASK_RESPONSE_TIMEOUT);
          context.requestRta();
          context.startTimer(EventType.WAIT_REQUEST_RTA_RESPONSE_TIMEOUT,
                             context.assignParams(),
                             Constant.TIMEOUT_WAIT_RTA_RESPONSE);
          context.switchState(Instance.of(WaitForRtaState.class));
        }
        break;
      case WAIT_TASK_RESPONSE_TIMEOUT:
        context.responseData();
        context.requestComplete();
        break;
      default:
        log.error("can't handel event:{}", eventType);
    }
  }
}
