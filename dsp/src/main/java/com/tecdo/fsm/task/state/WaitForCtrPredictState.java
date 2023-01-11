package com.tecdo.fsm.task.state;

import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.domain.biz.response.CtrResponse;
import com.tecdo.fsm.task.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 该状态内会计算出价
 *
 * Created by Zeki on 2023/1/4
 **/
@Slf4j
@Component
public class WaitForCtrPredictState implements ITaskState {

    @Autowired
    private MessageQueue messageQueue;
    @Autowired
    private WaitForCalcPriceState waitForCalcPriceState;

    @Override
    public void handleEvent(EventType eventType, Params params, Task task) {
        switch (eventType) {
            case CTR_PREDICT_FINISH:
                task.cancelTimer();
                Params taskParams = null;
                List<CtrResponse> ctrResponses =  taskParams.get(ParamKey.CTR_PREDICT_KEY);
                
                task.switchState(waitForCalcPriceState);
                break;
            case CTR_PREDICT_ERROR:
                task.cancelTimer();
                break;
            default:
                log.error("Task can't handle event, type: {}", eventType);
        }
    }
}
