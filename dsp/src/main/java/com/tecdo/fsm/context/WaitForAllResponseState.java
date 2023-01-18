package com.tecdo.fsm.context;

import com.tecdo.common.Params;
import com.tecdo.constant.Constant;
import com.tecdo.constant.EventType;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitForAllResponseState implements IContextState {

  private WaitForRtaState waitForRtaState;

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
          context.switchState(waitForRtaState);
        }
        break;
      case WAIT_TASK_RESPONSE_TIMEOUT:
        context.responseData();
        context.requestComplete();
        break;
      default:
        context.dispatchToTask(eventType, params);
    }
  }
}
