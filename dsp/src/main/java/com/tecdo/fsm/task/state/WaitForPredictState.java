package com.tecdo.fsm.task.state;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.fsm.task.Task;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 该状态内会计算出价
 * <p>
 * 受限点击成本：
 * cpc = AdGroup::optPrice * pctr * 1000
 * cpm = AdGroup::optPrice
 *
 * <p>
 * Created by Zeki on 2023/1/4
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class WaitForPredictState implements ITaskState {

  private final WaitForCalcPriceState waitForCalcPriceState;
  private final WaitForRecycleState waitForRecycleState;

  @Value("${pac.timeout.task.ad.calc-price}")
  private long timeoutCalcPrice;

  @Override
  public void handleEvent(EventType eventType, Params params, Task task) {
    switch (eventType) {
      case PREDICT_FINISH:
        task.savePredictResponse(params.get(ParamKey.ADS_PREDICT_RESPONSE));
        boolean receiveAllPredictResponse = task.isReceiveAllPredictResponse();
        if (receiveAllPredictResponse) {
          task.tick("task-calc-price");
          task.cancelTimer(EventType.PREDICT_TIMEOUT);
          task.calcPrice();
          task.startTimer(EventType.CALC_CPC_TIMEOUT, params, timeoutCalcPrice);
          task.switchState(waitForCalcPriceState);
        }
        break;
      case PREDICT_ERROR:
        task.cancelTimer(EventType.PREDICT_TIMEOUT);
        task.notifyFailed();
        task.switchState(waitForRecycleState);
        break;
      case PREDICT_TIMEOUT:
        task.notifyFailed();
        task.switchState(waitForCalcPriceState);
        break;
      default:
        log.error("Task can't handle event, type: {}", eventType);
    }
  }

}
