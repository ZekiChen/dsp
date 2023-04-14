package com.tecdo.fsm.context;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitForRtaState implements IContextState {

  private final WaitForSortState waitForSortState;
  private final WaitForRecycleState waiForRecycleState;

  @Value("${pac.timeout.context.bid-price-sort}")
  private long sortTimeout;

  @Override
  public void handleEvent(EventType eventType, Params params, Context context) {
    switch (eventType) {
      case REQUEST_RTA_RESPONSE:
        if (++context.rtaResponseCount == context.rtaResponseNeed) {
          context.cancelTimer(EventType.WAIT_REQUEST_RTA_RESPONSE_TIMEOUT);
          context.rtaResponseCount = 0;
          context.saveRtaResponse(params);
          if (context.checkResponse()) {
            context.sort();
            context.startTimer(EventType.WAIT_SORT_AD_TIMEOUT,
                    context.assignParams(),
                    sortTimeout);
            context.switchState(waitForSortState);
          } else {
            context.switchState(waiForRecycleState);
            context.responseData();
            context.requestComplete();
          }
        }
        break;
      case WAIT_REQUEST_RTA_RESPONSE_TIMEOUT:
        context.switchState(waiForRecycleState);
        context.responseData();
        context.requestComplete();
        break;
      case WAIT_REQUEST_RTA_RESPONSE_ERROR:
        context.cancelTimer(EventType.WAIT_REQUEST_RTA_RESPONSE_TIMEOUT);
        context.switchState(waiForRecycleState);
        context.responseData();
        context.requestComplete();
        break;
      default:
        log.error("can't handel event:{}", eventType);
    }
  }
}
