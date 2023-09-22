package com.tecdo.fsm.context.state;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.fsm.context.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitForDistinctState implements IContextState {

  private final WaitForRecycleState waiForRecycleState;

  @Override
  public void handleEvent(EventType eventType, Params params, Context context) {
    switch (eventType) {
      case DISTINCT_AD_RESPONSE:
        context.tick("context-response");
        context.cancelTimer(EventType.DISTINCT_AD_TIMEOUT);
        context.saveDistinctResponse(params);
        context.switchState(waiForRecycleState);
        context.responseData();
        context.requestComplete();
        break;
      case DISTINCT_AD_TIMEOUT:
        context.switchState(waiForRecycleState);
        context.responseData();
        context.requestComplete();
        break;
      default:
        log.error("can't handel event:{}", eventType);
    }
  }
}
