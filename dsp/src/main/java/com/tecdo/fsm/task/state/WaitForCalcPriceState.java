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
import java.util.stream.Collectors;

/**
 * 该状态内会过滤低于底价的广告
 *
 * Created by Zeki on 2023/1/4
 **/
@Slf4j
@Component
public class WaitForCalcPriceState implements ITaskState {

    @Autowired
    private MessageQueue messageQueue;

    @Override
    public void handleEvent(EventType eventType, Params params, Task task) {
        switch (eventType) {
            case CALC_CPC_FINISH:
                ThreadPool.getInstance().execute(() -> {
                    Map<Integer, AdDTO> adDTOMap = params.get(ParamKey.ADS_IMP_KEY);
                    adDTOMap = adDTOMap.values().stream().filter(e -> e.getBidPrice() > task.getImp().getBidfloor())
                            .collect(Collectors.toMap(e -> e.getAd().getId(), e -> e));
                    params.put(ParamKey.ADS_IMP_KEY, adDTOMap);
                });
                task.startTimer(Constant.TIMEOUT_PRICE_FILTER);
                break;
            case CALC_CPC_ERROR:
                task.cancelTimer();
                break;
            case CALC_CPC_TIMEOUT:

                break;
            default:
                log.error("Task can't handle event, type: {}", eventType);
        }
    }
}
