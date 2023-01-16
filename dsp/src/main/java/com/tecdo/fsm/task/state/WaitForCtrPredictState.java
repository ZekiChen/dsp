package com.tecdo.fsm.task.state;

import com.tecdo.common.Params;
import com.tecdo.common.ThreadPool;
import com.tecdo.constant.Constant;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.fsm.task.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 该状态内会计算出价
 * 受限点击成本：出价cpc = mcpc * pctr * 1000
 * <p>
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
                Map<Integer, AdDTO> adDTOMap = taskParams.get(ParamKey.ADS_IMP_KEY);
                try {
                    ThreadPool.getInstance().execute(() -> {
                        adDTOMap.values().forEach(e -> e.setBidPrice(e.getAdGroup().getOptPrice() * e.getPCtr() * 1000));
                        taskParams.put(ParamKey.ADS_IMP_KEY, adDTOMap);
                        messageQueue.putMessage(EventType.CALC_CPC_FINISH);
                    });
                } catch (Exception e) {
                    log.error("calculate cpc error: {}", e.getMessage());
                    messageQueue.putMessage(EventType.CALC_CPC_ERROR);
                }
                task.startTimer(Constant.TIMEOUT_CALC_PRICE);
                task.switchState(waitForCalcPriceState);
                break;
            case CTR_PREDICT_ERROR:
                task.cancelTimer();
                // 本次 bid 不参与
                break;
            case CTR_PREDICT_TIMEOUT:
                // TODO 重试？
                break;
            default:
                log.error("Task can't handle event, type: {}", eventType);
        }
    }
}
