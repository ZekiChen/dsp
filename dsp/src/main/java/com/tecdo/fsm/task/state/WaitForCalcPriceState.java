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
 * 该状态内会过滤低于底价的广告
 * <p>
 * Created by Zeki on 2023/1/4
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class WaitForCalcPriceState implements ITaskState {

  private final WaitForPriceFilterState waitForPriceFilterState;
  private final WaitForRecycleState waitForRecycleState;

  @Value("${pac.timeout.task.ad.price-filter}")
  private long timeoutPriceFilter;

  @Override
  public void handleEvent(EventType eventType, Params params, Task task) {
    switch (eventType) {
      case CALC_CPC_FINISH:
        if (task.calcPriceResponseFinish()) {
          task.tick("task-price-filter");
          task.cancelTimer(EventType.CALC_CPC_TIMEOUT);
          task.priceFilter(params.get(ParamKey.ADS_CALC_PRICE_RESPONSE));
          task.startTimer(EventType.PRICE_FILTER_TIMEOUT, params, timeoutPriceFilter);
          task.switchState(waitForPriceFilterState);
        }
        break;
      case CALC_CPC_ERROR:
        task.cancelTimer(EventType.CALC_CPC_TIMEOUT);
        task.notifyFailed();
        task.switchState(waitForRecycleState);
        break;
      case CALC_CPC_TIMEOUT:
        task.notifyFailed();
        task.switchState(waitForRecycleState);
        break;
      default:
        log.error("Task can't handle event, type: {}", eventType);
    }
  }
}
