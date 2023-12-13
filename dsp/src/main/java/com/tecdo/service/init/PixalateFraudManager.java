package com.tecdo.service.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.PixalateFraudTolerance;
import com.tecdo.adm.api.delivery.mapper.PixalateFraudToleranceMapper;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.core.launch.thread.ThreadPool;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/12/8
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PixalateFraudManager extends ServiceImpl<PixalateFraudToleranceMapper, PixalateFraudTolerance> {

    private final SoftTimer softTimer;
    private final MessageQueue messageQueue;
    private final ThreadPool threadPool;

    private State currentState = State.INIT;
    private long timerId;

    private Map<String, Double> fraudMap;

    @Value("${pac.timeout.load.db.default}")
    private long loadTimeout;
    @Value("${pac.interval.reload.db.default}")
    private long reloadInterval;

    /**
     * 从 DB 加载 PixalateFraudTolerance 集合，每 5 分钟刷新一次缓存
     */
    public double getProbability(String fraudType){
        return fraudMap.get(fraudType);
    }

    @AllArgsConstructor
    private enum State {
        INIT(1, "init"),
        WAIT_INIT_RESPONSE(2, "waiting init response"),
        RUNNING(3,"init success, now is running"),
        UPDATING(4,"updating");

        private int code;
        private String desc;

        @Override
        public String toString() {
            return code + " - " + desc;
        }
    }

    public void init(Params params) {
        messageQueue.putMessage(EventType.PIXALATE_FRAUD_LOAD, params);
    }

    private void startReloadTimeoutTimer(Params params) {
        timerId = softTimer.startTimer(EventType.PIXALATE_FRAUD_LOAD_TIMEOUT, params, loadTimeout);
    }

    private void cancelReloadTimeoutTimer() {
        softTimer.cancel(timerId);
    }

    private void startNextReloadTimer(Params params) {
        softTimer.startTimer(EventType.PIXALATE_FRAUD_LOAD, params, reloadInterval);
    }

    public void switchState(State state) {
        this.currentState = state;
    }

    public void handleEvent(EventType eventType, Params params) {
        switch (eventType) {
            case PIXALATE_FRAUD_LOAD:
                handleReload(params);
                break;
            case PIXALATE_FRAUD_LOAD_RESPONSE:
                handleResponse(params);
                break;
            case PIXALATE_FRAUD_LOAD_ERROR:
                handleError(params);
                break;
            case PIXALATE_FRAUD_LOAD_TIMEOUT:
                handleTimeout(params);
                break;
            default:
                log.error("Can't handle event, type: {}", eventType);
        }
    }

    private void handleReload(Params params) {
        switch (currentState) {
            case INIT:
            case RUNNING:
                threadPool.execute(() -> {
                    try {
                        LambdaQueryWrapper<PixalateFraudTolerance> wrapper =
                                Wrappers.<PixalateFraudTolerance>lambdaQuery().eq(PixalateFraudTolerance::getStatus, 1);
                        Map<String, Double> fraudMap = list(wrapper).stream().collect(Collectors.toMap(
                                PixalateFraudTolerance::getFraudType, PixalateFraudTolerance::getProbability));
                        params.put(ParamKey.PIXALATE_FRAUD_CACHE_KEY, fraudMap);
                        messageQueue.putMessage(EventType.PIXALATE_FRAUD_LOAD_RESPONSE, params);
                    } catch (Exception e) {
                        log.error("pixalate fraud load failure from db", e);
                        messageQueue.putMessage(EventType.PIXALATE_FRAUD_LOAD_ERROR, params);
                    }
                });
                startReloadTimeoutTimer(params);
                switchState(currentState == State.INIT ? State.WAIT_INIT_RESPONSE : State.UPDATING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleResponse(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
                messageQueue.putMessage(EventType.ONE_DATA_READY);
            case UPDATING:
                cancelReloadTimeoutTimer();
                this.fraudMap = params.get(ParamKey.PIXALATE_FRAUD_CACHE_KEY);
                log.info("pixalate fraud load success, size: {}", fraudMap.size());
                startNextReloadTimer(params);
                switchState(State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleError(Params params) {
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

    private void handleTimeout(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
            case UPDATING:
                log.error("timeout load pixalate fraud");
                startNextReloadTimer(params);
                switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

}
