package com.tecdo.fsm.context.state;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.fsm.context.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitForAllResponseState implements IContextState {

  private final WaitForDistinctState waitForDistinctState;
  private final WaitForRecycleState waiForRecycleState;

  @Value("${pac.timeout.context.ad-distinct}")
  private long distinctTimeout;

  @Override
  public void handleEvent(EventType eventType, Params params, Context context) {
    switch (eventType) {
      case BID_TASK_FINISH:
        context.saveTaskResponse(params);
        if (context.isReceiveAllTaskResponse()) {
          context.tick("context-distinct");
          context.cancelTimer(EventType.WAIT_TASK_RESPONSE_TIMEOUT);
          context.distinct();
          context.startTimer(EventType.DISTINCT_AD_TIMEOUT, context.assignParams(), distinctTimeout);
          context.switchState(waitForDistinctState);
        }
        break;
      case WAIT_TASK_RESPONSE_TIMEOUT:
        context.switchState(waiForRecycleState);
        context.responseData();
        context.requestComplete();
        break;
      case TASK_START:
      case ADS_RECALL_FINISH:
      case ADS_RECALL_ERROR:
      case ADS_RECALL_TIMEOUT:
      case PREDICT_FINISH:
      case PREDICT_ERROR:
      case PREDICT_TIMEOUT:
      case CALC_CPC_FINISH:
      case CALC_CPC_ERROR:
      case CALC_CPC_TIMEOUT:
      case PRICE_FILTER_FINISH:
      case PRICE_FILTER_TIMEOUT:
      case REQUEST_RTA_RESPONSE:
      case WAIT_REQUEST_RTA_RESPONSE_ERROR:
      case WAIT_REQUEST_RTA_RESPONSE_TIMEOUT:
      case SORT_AD_RESPONSE:
      case WAIT_SORT_AD_TIMEOUT:
        context.dispatchToTask(eventType, params);
        break;
      case BID_TASK_FAILED:
        context.cancelTimer(EventType.WAIT_TASK_RESPONSE_TIMEOUT);
        context.switchState(waiForRecycleState);
        context.responseData();
        context.requestComplete();
        break;
      default:
        log.error("can't handel event:{}", eventType);
    }
  }
}
