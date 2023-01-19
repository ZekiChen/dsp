package com.tecdo.fsm.context;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitForSortState implements IContextState {

  private WaitForRecycleState waiForRecycleState;

  @Override
  public void handleEvent(EventType eventType, Params params, Context context) {
    switch (eventType) {
      case SORT_AD_RESPONSE:
        context.cancelTimer(EventType.WAIT_SORT_AD_TIMEOUT);
        context.saveSortAdResponse(params);
        context.switchState(waiForRecycleState);
        context.responseData();
        context.requestComplete();
        break;
      case WAIT_SORT_AD_TIMEOUT:
        context.switchState(waiForRecycleState);
        context.responseData();
        context.requestComplete();
        break;
      default:
        log.error("can't handel event:{}", eventType);
    }
  }
}
