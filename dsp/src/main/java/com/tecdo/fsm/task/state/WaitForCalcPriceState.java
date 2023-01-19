package com.tecdo.fsm.task.state;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.fsm.task.Task;

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

  @Override
  public void handleEvent(EventType eventType, Params params, Task task) {
    switch (eventType) {
      case CALC_CPC_FINISH:
        task.cancelTimer(EventType.CALC_CPC_TIMEOUT);
        task.filerAdAndNotify(params.get(ParamKey.ADS_CALC_PRICE_RESPONSE));
        break;
      case CALC_CPC_ERROR:
        task.cancelTimer(EventType.CALC_CPC_TIMEOUT);
        break;
      case CALC_CPC_TIMEOUT:

        break;
      default:
        log.error("Task can't handle event, type: {}", eventType);
    }
  }
}
