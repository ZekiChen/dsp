package com.tecdo.fsm.context;

import com.tecdo.common.Instance;
import com.tecdo.common.Params;
import com.tecdo.constant.Constant;
import com.tecdo.constant.EventType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WaitForRtaState implements IContextState {
  @Override
  public void handleEvent(EventType eventType, Params params, Context context) {
    switch (eventType) {
      case REQUEST_RTA_RESPONSE:
        context.cancelTimer(EventType.WAIT_REQUEST_RTA_RESPONSE_TIMEOUT);
        context.saveRtaResponse(params);
        context.sort();
        context.startTimer(EventType.WAIT_SORT_AD_TIMEOUT,
                           context.assignParams(),
                           Constant.TEN_MILLIS);
        context.switchState(Instance.of(WaitForSortState.class));
        break;
      case WAIT_REQUEST_RTA_RESPONSE_TIMEOUT:
        context.responseData();
        context.requestComplete();
        break;
      case WAIT_REQUEST_RTA_RESPONSE_ERROR:
        context.cancelTimer(EventType.WAIT_REQUEST_RTA_RESPONSE_TIMEOUT);
        context.responseData();
        context.requestComplete();
        break;
      default:
        log.error("can't handel event:{}", eventType);
    }
  }
}