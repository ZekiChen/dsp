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

  private WaitForRecycleState waiForRecycleState;

  @Override
  public void handleEvent(EventType eventType, Params params, Context context) {
    switch (eventType) {
      case BID_TASK_FINISH:
        context.saveTaskResponse(params);
        boolean receiveAllTaskResponse = context.isReceiveAllTaskResponse();
        if (receiveAllTaskResponse) {
          context.cancelTimer(EventType.WAIT_TASK_RESPONSE_TIMEOUT);
          if (context.checkTaskResponse()) {
            context.requestRta();
            context.startTimer(EventType.WAIT_REQUEST_RTA_RESPONSE_TIMEOUT,
                               context.assignParams(),
                               Constant.TIMEOUT_WAIT_RTA_RESPONSE);
            context.switchState(waitForRtaState);
          } else {
            context.switchState(waiForRecycleState);
            context.responseData();
            context.requestComplete();
          }
        }
        break;
      // 这里简单处理，对于多个task，要求全部成功，只要超时或者一个task失败，则认为整个请求失败了
      case WAIT_TASK_RESPONSE_TIMEOUT:
        context.switchState(waiForRecycleState);
        context.responseData();
        context.requestComplete();
        break;
      case BID_TASK_FAILED:
        context.cancelTimer(EventType.WAIT_TASK_RESPONSE_TIMEOUT);
        context.switchState(waiForRecycleState);
        context.responseData();
        context.requestComplete();
        break;
      case TASK_START:
      case ADS_RECALL_FINISH:
      case ADS_RECALL_ERROR:
      case ADS_RECALL_TIMEOUT:
      case CTR_PREDICT_FINISH:
      case CTR_PREDICT_ERROR:
      case CTR_PREDICT_TIMEOUT:
      case CALC_CPC_FINISH:
      case CALC_CPC_ERROR:
      case CALC_CPC_TIMEOUT:
        context.dispatchToTask(eventType, params);
        break;
      default:
        log.error("can't handel event:{}", eventType);
    }
  }
}
