package com.tecdo.fsm.task.state;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.fsm.task.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 该状态内会计算出价
 *
 * 受限点击成本：
 * cpc = mcpc（即AdGroup::optPrice）* pctr（百分比）* 10
 * cpm = 即 AdGroup::optPrice
 *
 * <p>
 * Created by Zeki on 2023/1/4
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class WaitForCtrPredictState implements ITaskState {

  private final WaitForCalcPriceState waitForCalcPriceState;
  private final WaitForRecycleState waitForRecycleState;

  @Value("${pac.timeout.task.ad.calc-price}")
  private long timeoutCalcPrice;

  @Override
  public void handleEvent(EventType eventType, Params params, Task task) {
    switch (eventType) {
      case CTR_PREDICT_FINISH:
        task.cancelTimer(EventType.CTR_PREDICT_TIMEOUT);
        Map<Integer, AdDTOWrapper> adDTOMap = params.get(ParamKey.ADS_P_CTR_RESPONSE);
        task.calcPrice(adDTOMap);
        task.startTimer(EventType.CALC_CPC_TIMEOUT, params, timeoutCalcPrice);
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
