package com.tecdo.service.init;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.adm.api.delivery.entity.RtaInfo;
import com.tecdo.adm.api.delivery.mapper.RtaInfoMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2022/12/27
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class RtaInfoManager extends ServiceImpl<RtaInfoMapper, RtaInfo> {

    private final SoftTimer softTimer;
    private final MessageQueue messageQueue;
    private final ThreadPool threadPool;

    private State currentState = State.INIT;
    private long timerId;

    private Map<Integer, RtaInfo> rtaInfoMap;

    @Value("${pac.timeout.load.db.default}")
    private long loadTimeout;
    @Value("${pac.interval.reload.db.default}")
    private long reloadInterval;

    /**
     * 从 DB 加载 rta_info 集合，每 5 分钟刷新一次缓存
     */
    public Map<Integer, RtaInfo> getRtaInfoMap() {
        return this.rtaInfoMap;
    }

    public RtaInfo getRtaInfo(Integer advId) {
        return this.rtaInfoMap.get(advId);
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
        messageQueue.putMessage(EventType.RTA_INFOS_LOAD, params);
    }

    private void startReloadTimeoutTimer(Params params) {
        timerId = softTimer.startTimer(EventType.RTA_INFOS_LOAD_TIMEOUT, params, loadTimeout);
    }

    private void cancelReloadTimeoutTimer() {
        softTimer.cancel(timerId);
    }

    private void startNextReloadTimer(Params params) {
        softTimer.startTimer(EventType.RTA_INFOS_LOAD, params, reloadInterval);
    }

    public void switchState(State state) {
        this.currentState = state;
    }

    public void handleEvent(EventType eventType, Params params) {
        switch (eventType) {
            case RTA_INFOS_LOAD:
                handleRtaInfosReload(params);
                break;
            case RTA_INFOS_LOAD_RESPONSE:
                handleRtaInfosResponse(params);
                break;
            case RTA_INFOS_LOAD_ERROR:
                handleRtaInfosError(params);
                break;
            case RTA_INFOS_LOAD_TIMEOUT:
                handleRtaInfosTimeout(params);
                break;
            default:
                log.error("Can't handle event, type: {}", eventType);
        }
    }

    private void handleRtaInfosReload(Params params) {
        switch (currentState) {
            case INIT:
            case RUNNING:
                threadPool.execute(() -> {
                    try {
                        Map<Integer, RtaInfo> rtaInfoMap = list().stream().collect(Collectors.toMap(RtaInfo::getAdvMemId, e -> e));
                        params.put(ParamKey.RTA_INFOS_CACHE_KEY, rtaInfoMap);
                        messageQueue.putMessage(EventType.RTA_INFOS_LOAD_RESPONSE, params);
                    } catch (Exception e) {
                        log.error("rta infos load failure from db", e);
                        messageQueue.putMessage(EventType.RTA_INFOS_LOAD_ERROR, params);
                    }
                });
                startReloadTimeoutTimer(params);
                switchState(currentState == State.INIT ? State.WAIT_INIT_RESPONSE : State.UPDATING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleRtaInfosResponse(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
                messageQueue.putMessage(EventType.ONE_DATA_READY);
            case UPDATING:
                cancelReloadTimeoutTimer();
                this.rtaInfoMap = params.get(ParamKey.RTA_INFOS_CACHE_KEY);
                log.info("rta infos load success, size: {}", rtaInfoMap.size());
                startNextReloadTimer(params);
                switchState(State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleRtaInfosError(Params params) {
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

    private void handleRtaInfosTimeout(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
            case UPDATING:
                log.error("timeout load rta info");
                startNextReloadTimer(params);
                switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }
}
