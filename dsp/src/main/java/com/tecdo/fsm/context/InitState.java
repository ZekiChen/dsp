package com.tecdo.fsm.context;

import com.tecdo.common.Params;
import com.tecdo.constant.Constant;
import com.tecdo.constant.EventType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitState implements IContextState {

  private WaitForAllResponseState waitForAllResponseState;

  private static Logger requestLog = LoggerFactory.getLogger("request_log");

  @Override
  public void handleEvent(EventType eventType, Params params, Context context) {
    switch (eventType) {
      case RECEIVE_BID_REQUEST:
        context.logBidRequest();
        context.handleBidRequest();
        context.startTimer(EventType.WAIT_TASK_RESPONSE_TIMEOUT,
                           context.assignParams(),
                           Constant.TIMEOUT_WAIT_TASK_RESPONSE);
        context.switchState(waitForAllResponseState);

        break;
      default:
        log.error("can't handel event:{}", eventType);
    }
  }
}
