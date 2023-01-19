package com.tecdo.fsm.task.state;

import com.tecdo.common.Params;
import com.tecdo.constant.Constant;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.fsm.task.Task;

import org.springframework.stereotype.Component;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 该状态内会进行 CTR 预估
 *
 * Created by Zeki on 2023/1/4
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class WaitForRecallState implements ITaskState {

    private final WaitForCtrPredictState waitForCtrPredictState;

    @Override
    public void handleEvent(EventType eventType, Params params, Task task) {
        switch (eventType) {
            case ADS_RECALL_FINISH:
                task.cancelTimer(EventType.ADS_RECALL_TIMEOUT);
                Map<Integer, AdDTOWrapper> adDTOMap = params.get(ParamKey.ADS_RECALL_RESPONSE);
                task.callCtr3Api(adDTOMap);
                task.startTimer(EventType.CTR_PREDICT_TIMEOUT, params, Constant.TIMEOUT_PRE_DICT);
                task.switchState(waitForCtrPredictState);
                break;
            case ADS_RECALL_ERROR:
                task.cancelTimer(EventType.ADS_RECALL_TIMEOUT);
                // 本次 bid 不参与
                break;
            case ADS_RECALL_TIMEOUT:
                // TODO 重试广告召回？次数？还是本次bid不参与？
                break;
            default:
                log.error("Task can't handle event, type: {}", eventType);
        }
    }

}
