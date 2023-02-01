package com.tecdo.fsm.task.state;

import com.tecdo.common.Params;
import com.tecdo.constant.Constant;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.fsm.task.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 该状态内会计算出价
 * TODO cpc cpm
 * 受限点击成本：出价cpc = mcpc * pctr * 1000
 * <p>
 * Created by Zeki on 2023/1/4
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class WaitForCtrPredictState implements ITaskState {

  private final WaitForCalcPriceState waitForCalcPriceState;

  private final WaitForRecycleState waitForRecycleState;

  @Override
  public void handleEvent(EventType eventType, Params params, Task task) {
    switch (eventType) {
      case CTR_PREDICT_FINISH:
        task.cancelTimer(EventType.CTR_PREDICT_TIMEOUT);
        Map<Integer, AdDTOWrapper> adDTOMap = params.get(ParamKey.ADS_P_CTR_RESPONSE);
        task.calcPrice(adDTOMap);
        task.startTimer(EventType.CALC_CPC_TIMEOUT, params, Constant.TIMEOUT_CALC_PRICE);
        task.switchState(waitForCalcPriceState);
        break;
      case CTR_PREDICT_ERROR:
        task.cancelTimer(EventType.CTR_PREDICT_TIMEOUT);
        task.notifyFailed();
        task.switchState(waitForRecycleState);
        break;
      case CTR_PREDICT_TIMEOUT:
        task.notifyFailed();
        task.switchState(waitForCalcPriceState);
        break;
      default:
        log.error("Task can't handle event, type: {}", eventType);
    }
  }

}
