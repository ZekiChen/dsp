package com.tecdo.fsm.context;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component("ContextInitState")
@RequiredArgsConstructor
public class InitState implements IContextState {

  private final WaitForAllResponseState waitForAllResponseState;

  @Value("${pac.timeout.context.task.response}")
  private long timeoutTaskResponse;

  @Override
  public void handleEvent(EventType eventType, Params params, Context context) {
    switch (eventType) {
      case RECEIVE_BID_REQUEST:
        context.tick("context-ad-recall");
        context.handleBidRequest();
        context.startTimer(EventType.WAIT_TASK_RESPONSE_TIMEOUT,
                           context.assignParams(),
                           timeoutTaskResponse);
        context.switchState(waitForAllResponseState);
        break;
      default:
        log.error("can't handel event:{}", eventType);
    }
  }
}
