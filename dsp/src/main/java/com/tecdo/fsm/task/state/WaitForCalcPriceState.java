package com.tecdo.fsm.task.state;

import com.tecdo.common.Params;
import com.tecdo.common.ThreadPool;
import com.tecdo.constant.Constant;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.fsm.task.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class WaitForCalcPriceState implements ITaskState {

    private final MessageQueue messageQueue;

    @Override
    public void handleEvent(EventType eventType, Params params, Task task) {
        switch (eventType) {
            case CALC_CPC_FINISH:
                task.cancelTimer(EventType.CALC_CPC_TIMEOUT);
                ThreadPool.getInstance().execute(() -> {
                    Map<Integer, AdDTO> adDTOMap = params.get(ParamKey.ADS_IMP_KEY);
                    adDTOMap = adDTOMap.values().stream().filter(e -> e.getBidPrice() > task.getImp().getBidfloor())
                            .collect(Collectors.toMap(e -> e.getAd().getId(), e -> e));
                    params.put(ParamKey.ADS_IMP_KEY, adDTOMap);
                    messageQueue.putMessage(EventType.BID_PRICE_FILTER_FINISH);
                });
                task.startTimer(EventType.BID_PRICE_FILTER_TIMEOUT, params, Constant.TIMEOUT_PRICE_FILTER);
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
