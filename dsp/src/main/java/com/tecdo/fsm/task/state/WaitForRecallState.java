package com.tecdo.fsm.task.state;

import com.tecdo.common.Params;
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
 * 该状态内会进行 CTR 预估
 * <p>
 * Created by Zeki on 2023/1/4
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class WaitForRecallState implements ITaskState {

  private final WaitForCtrPredictState waitForCtrPredictState;
  private final WaitForRecycleState waitForRecycleState;

  @Value("${pac.timeout.task.ad.p-ctr}")
  private long timeoutPCtr;

  @Override
  public void handleEvent(EventType eventType, Params params, Task task) {
    switch (eventType) {
      case ADS_RECALL_FINISH:
        task.cancelTimer(EventType.ADS_RECALL_TIMEOUT);
        Map<Integer, AdDTOWrapper> adDTOMap = params.get(ParamKey.ADS_RECALL_RESPONSE);
        if (adDTOMap.isEmpty()) {
          task.filerAdAndNotifySuccess(adDTOMap);
          task.switchState(waitForRecycleState);
          return;
        }
        task.callCtr3Api(adDTOMap);
        task.startTimer(EventType.CTR_PREDICT_TIMEOUT, params, timeoutPCtr);
        task.switchState(waitForCtrPredictState);
        break;
      case ADS_RECALL_ERROR:
        task.cancelTimer(EventType.ADS_RECALL_TIMEOUT);
        task.notifyFailed();
        task.switchState(waitForRecycleState);
        break;
      case ADS_RECALL_TIMEOUT:
        task.notifyFailed();
        task.switchState(waitForRecycleState);
        break;
      default:
        log.error("Task can't handle event, type: {}", eventType);
    }
  }

}
