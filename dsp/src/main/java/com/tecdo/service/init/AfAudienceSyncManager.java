package com.tecdo.service.init;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.core.launch.thread.ThreadPool;
import com.tecdo.adm.api.audience.entity.AfSync;
import com.tecdo.mapper.AfSyncMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author sisyphus.su
 * @description: appflyer audience 同步表
 * @date: 2023-05-10 11:20
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class AfAudienceSyncManager extends ServiceImpl<AfSyncMapper, AfSync> {

    private final SoftTimer softTimer;
    private final MessageQueue messageQueue;
    private final ThreadPool threadPool;

    private State currentState = State.INIT;
    private long timerId;
    private boolean initFinish;

    private Map<Integer, List<AfSync>> afSyncMap;

    @Value("${pac.timeout.load.db.default}")
    private long loadTimeout;
    @Value("${pac.interval.reload.db.default}")
    private long reloadInterval;


    public Map<Integer, List<AfSync>> getAfSyncMap() {
        return afSyncMap;
    }

    public List<AfSync> getAfSyncList(Integer containerId) {
        return afSyncMap.get(containerId);
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
        messageQueue.putMessage(EventType.AF_AUDIENCE_SYNC_TABLE_LOAD, params);
    }

    private void startReloadTimeoutTimer(Params params) {
        timerId = softTimer.startTimer(EventType.AF_AUDIENCE_SYNC_LOAD_TIMEOUT, params, loadTimeout);
    }

    private void cancelReloadTimeoutTimer() {
        softTimer.cancel(timerId);
    }

    private void startNextReloadTimer(Params params) {
        softTimer.startTimer(EventType.AF_AUDIENCE_SYNC_TABLE_LOAD, params, reloadInterval);
    }

    public void switchState(State state) {
        this.currentState = state;
    }

    public void handleEvent(EventType eventType, Params params) {
        switch (eventType) {
            case AF_AUDIENCE_SYNC_TABLE_LOAD:
                handleAfAudienceSyncReload(params);
                break;
            case AF_AUDIENCE_SYNC_LOAD_RESPONSE:
                handleAfAudienceSyncResponse(params);
                break;
            case AF_AUDIENCE_SYNC_LOAD_ERROR:
                handleAfAudienceSyncError(params);
                break;
            case AF_AUDIENCE_SYNC_LOAD_TIMEOUT:
                handleAfAudienceSyncTimeout(params);
                break;
            default:
                log.error("Can't handle event, type: {}", eventType);
        }
    }

    private void handleAfAudienceSyncReload(Params params) {
        switch (currentState) {
            case INIT:
            case RUNNING:
                threadPool.execute(() -> {
                    try {
                        List<AfSync> afSyncList = list();
                        Map<Integer, List<AfSync>> afSyncMap =
                        afSyncList
                            .stream()
                            .filter(v -> v.getHasSync() && v.getIsEnable())
                            .collect(Collectors.groupingBy(AfSync::getContainerId));
                        params.put(ParamKey.AF_AUDIENCE_SYNC_KEY, afSyncMap);
                        messageQueue.putMessage(EventType.AF_AUDIENCE_SYNC_LOAD_RESPONSE, params);
                    } catch (Exception e) {
                        log.error("af audience sync load failure from db", e);
                        messageQueue.putMessage(EventType.AF_AUDIENCE_SYNC_LOAD_ERROR, params);
                    }
                });
                startReloadTimeoutTimer(params);
                switchState(currentState == State.INIT ? State.WAIT_INIT_RESPONSE : State.UPDATING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleAfAudienceSyncResponse(Params params) {
        if (!initFinish) {
            messageQueue.putMessage(EventType.ONE_DATA_READY);
            initFinish = true;
        }
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
            case UPDATING:
                cancelReloadTimeoutTimer();
                this.afSyncMap = params.get(ParamKey.AF_AUDIENCE_SYNC_KEY);
                log.info("af audience sync load success, size: {}", afSyncMap.size());
                startNextReloadTimer(params);
                switchState(State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }


    private void handleAfAudienceSyncError(Params params) {
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


    private void handleAfAudienceSyncTimeout(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
            case UPDATING:
                log.error("timeout load af audience");
                startNextReloadTimer(params);
                switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

}
