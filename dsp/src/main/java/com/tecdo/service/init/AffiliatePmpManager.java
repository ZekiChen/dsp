package com.tecdo.service.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.AffiliatePmp;
import com.tecdo.adm.api.delivery.mapper.AffiliatePmpMapper;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Elwin on 2023/12/11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliatePmpManager extends ServiceImpl<AffiliatePmpMapper, AffiliatePmp> {
    private final SoftTimer softTimer;
    private final MessageQueue messageQueue;
    private final ThreadPool threadPool;

    private State currentState = State.INIT;
    private long timerId;

    private Map<Integer, AffiliatePmp> affiliatePmpIdMap;

    @Value("${pac.timeout.load.db.default}")
    private long loadTimeout;
    @Value("${pac.interval.reload.db.default}")
    private long reloadInterval;

    /**
     * 从 DB 加载 affiliate_pmp 集合，每 5 分钟刷新一次缓存
     */
    public AffiliatePmp getAffiliatePmp(Integer id) {
        return affiliatePmpIdMap.get(id);
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
        messageQueue.putMessage(EventType.AFFILIATE_PMP_LOAD, params);
    }

    private void startReloadTimeoutTimer(Params params) {
        timerId = softTimer.startTimer(EventType.AFFILIATE_PMP_LOAD_TIMEOUT, params, loadTimeout);
    }

    private void cancelReloadTimeoutTimer() {
        softTimer.cancel(timerId);
    }

    private void startNextReloadTimer(Params params) {
        softTimer.startTimer(EventType.AFFILIATE_PMP_LOAD, params, reloadInterval);
    }

    public void switchState(State state) {
        this.currentState = state;
    }

    public void handleEvent(EventType eventType, Params params) {
        switch (eventType) {
            case AFFILIATE_PMP_LOAD:
                handleAffiliatePmpReload(params);
                break;
            case AFFILIATE_PMP_LOAD_RESPONSE:
                handleAffiliatePmpResponse(params);
                break;
            case AFFILIATE_PMP_LOAD_ERROR:
                handleAffiliatePmpError(params);
                break;
            case AFFILIATE_PMP_LOAD_TIMEOUT:
                handleAffiliatePmpTimeout(params);
                break;
            default:
                log.error("Can't handle event, type: {}", eventType);
        }
    }

    private void handleAffiliatePmpTimeout(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
            case UPDATING:
                log.error("timeout load affiliates");
                startNextReloadTimer(params);
                switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleAffiliatePmpError(Params params) {
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

    private void handleAffiliatePmpResponse(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
                messageQueue.putMessage(EventType.ONE_DATA_READY);
            case UPDATING:
                cancelReloadTimeoutTimer();
                this.affiliatePmpIdMap = params.get(ParamKey.AFFILIATE_PMP_ID_CACHE_KEY);
                log.info("affiliate pmp load success, size: {}", affiliatePmpIdMap.size());
                startNextReloadTimer(params);
                switchState(State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleAffiliatePmpReload(Params params) {
        switch (currentState) {
            case INIT:
            case RUNNING:
                threadPool.execute(() -> {
                    try {
                        affiliatePmpIdMap = list().stream().collect(Collectors.toMap(IdEntity::getId, e->e));
                        params.put(ParamKey.AFFILIATE_PMP_ID_CACHE_KEY, affiliatePmpIdMap);
                        messageQueue.putMessage(EventType.AFFILIATE_PMP_LOAD_RESPONSE, params);
                    } catch (Exception e) {
                        log.error("affiliate_pmp load failure from db", e);
                        messageQueue.putMessage(EventType.AFFILIATE_PMP_LOAD_ERROR, params);
                    }
                });
                startReloadTimeoutTimer(params);
                switchState(currentState == State.INIT ? State.WAIT_INIT_RESPONSE : State.UPDATING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }
}
