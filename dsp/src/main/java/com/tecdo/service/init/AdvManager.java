package com.tecdo.service.init;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.Adv;
import com.tecdo.adm.api.delivery.mapper.AdvMapper;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.starter.mp.entity.IdEntity;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/4/6
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdvManager extends ServiceImpl<AdvMapper, Adv> {

    private final SoftTimer softTimer;
    private final MessageQueue messageQueue;
    private final ThreadPool threadPool;

    private State currentState = State.INIT;
    private long timerId;

    private Map<Integer, String> advMap;

    @Value("${pac.timeout.load.db.default}")
    private long loadTimeout;
    @Value("${pac.interval.reload.db.default}")
    private long reloadInterval;

    /**
     * 从 DB 加载 adv 集合，每 5 分钟刷新一次缓存
     */
    public String getAdvName(Integer advId) {
        return advMap.get(advId);
    }

    @AllArgsConstructor
    private enum State {
        INIT(1, "init"),
        WAIT_INIT_RESPONSE(2, "waiting init response"),
        RUNNING(3, "init success, now is running"),
        UPDATING(4, "updating");

        private int code;
        private String desc;

        @Override
        public String toString() {
            return code + " - " + desc;
        }
    }

    public void init(Params params) {
        messageQueue.putMessage(EventType.ADV_LOAD, params);
    }

    private void startReloadTimeoutTimer(Params params) {
        timerId = softTimer.startTimer(EventType.ADV_LOAD_TIMEOUT, params, loadTimeout);
    }

    private void cancelReloadTimeoutTimer() {
        softTimer.cancel(timerId);
    }

    private void startNextReloadTimer(Params params) {
        softTimer.startTimer(EventType.ADV_LOAD, params, reloadInterval);
    }

    public void switchState(State state) {
        this.currentState = state;
    }

    public void handleEvent(EventType eventType, Params params) {
        switch (eventType) {
            case ADV_LOAD:
                handleAdvReload(params);
                break;
            case ADV_LOAD_RESPONSE:
                handleAdvResponse(params);
                break;
            case ADV_LOAD_ERROR:
                handleAdvError(params);
                break;
            case ADV_LOAD_TIMEOUT:
                handleAdvTimeout(params);
                break;
            default:
                log.error("Can't handle event, type: {}", eventType);
        }
    }

    private void handleAdvReload(Params params) {
        switch (currentState) {
            case INIT:
            case RUNNING:
                threadPool.execute(() -> {
                    try {
                        Map<Integer, String> advMap = list().stream().collect(Collectors.toMap(IdEntity::getId, Adv::getName));
                        params.put(ParamKey.ADV_CACHE_KEY, advMap);
                        messageQueue.putMessage(EventType.ADV_LOAD_RESPONSE, params);
                    } catch (Exception e) {
                        log.error("adv list load failure from db", e);
                        messageQueue.putMessage(EventType.ADV_LOAD_ERROR, params);
                    }
                });
                startReloadTimeoutTimer(params);
                switchState(currentState == State.INIT ? State.WAIT_INIT_RESPONSE : State.UPDATING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleAdvResponse(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
                messageQueue.putMessage(EventType.ONE_DATA_READY);
            case UPDATING:
                cancelReloadTimeoutTimer();
                this.advMap = params.get(ParamKey.ADV_CACHE_KEY);
                log.info("adv list load success, size: {}", advMap.size());
                startNextReloadTimer(params);
                switchState(State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleAdvError(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
            case UPDATING:
                cancelReloadTimeoutTimer();
                startNextReloadTimer(params);
                switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleAdvTimeout(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
            case UPDATING:
                startNextReloadTimer(params);
                switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

}
