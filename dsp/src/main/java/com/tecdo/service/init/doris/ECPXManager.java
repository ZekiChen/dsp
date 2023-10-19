package com.tecdo.service.init.doris;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.doris.dto.ECPX;
import com.tecdo.adm.api.doris.entity.Report;
import com.tecdo.adm.api.doris.mapper.ReportMapper;
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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/8/1
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ECPXManager extends ServiceImpl<ReportMapper, Report> {

    private final SoftTimer softTimer;
    private final MessageQueue messageQueue;
    private final ThreadPool threadPool;

    private State currentState = State.INIT;
    private long timerId;

    private Map<String, ECPX> eCPXMap;

    @Value("${pac.timeout.load.doris.report.ecpx}")
    private long loadTimeout;
    @Value("${pac.interval.reload.doris.report.ecpx}")
    private long reloadInterval;

    private static final ECPX EMPTY = new ECPX();

    /**
     * 从 Doris 加载 ECPX 集合，取历史前 3 天数据。每 1 小时刷新一次缓存
     */
    public Map<String, ECPX> geteCPXMap() {
        return this.eCPXMap;
    }

    public ECPX getECPX(String key) {
        return geteCPXMap().getOrDefault(key, EMPTY);
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
        messageQueue.putMessage(EventType.ECPX_LOAD, params);
    }

    private void startReloadTimeoutTimer(Params params) {
        timerId = softTimer.startTimer(EventType.ECPX_LOAD_TIMEOUT, params, loadTimeout);
    }

    private void cancelReloadTimeoutTimer() {
        softTimer.cancel(timerId);
    }

    private void startNextReloadTimer(Params params) {
        softTimer.startTimer(EventType.ECPX_LOAD, params, reloadInterval);
    }

    public void switchState(State state) {
        this.currentState = state;
    }

    public void handleEvent(EventType eventType, Params params) {
        switch (eventType) {
            case ECPX_LOAD:
                handleECPXReload(params);
                break;
            case ECPX_LOAD_RESPONSE:
                handleECPXResponse(params);
                break;
            case ECPX_LOAD_ERROR:
                handleECPXError(params);
                break;
            case ECPX_LOAD_TIMEOUT:
                handleECPXTimeout(params);
                break;
            default:
                log.error("Can't handle event, type: {}", eventType);
        }
    }

    private void handleECPXReload(Params params) {
        switch (currentState) {
            case INIT:
            case RUNNING:
                threadPool.execute(() -> {
                    try {
                        String startDay = DateUtil.offsetDay(new Date(), -3).toDateStr();
                        String endDay = DateUtil.offsetDay(new Date(), -1).toDateStr();
                        long startTime = System.currentTimeMillis();
                        List<ECPX> eCPXs = baseMapper.listECPX(startDay, endDay);
                        Map<String, ECPX> eCPXMap = eCPXs.stream().collect(Collectors.toMap(
                                eCPX -> eCPX.getCountry()
                                        .concat("_").concat(eCPX.getBundle())
                                        .concat("_").concat(eCPX.getAdFormat()),
                                eCPX -> eCPX));
                        log.info("eCPXs load time: {}s", (System.currentTimeMillis() - startTime) / 1000);
                        params.put(ParamKey.ECPX_CACHE_KEY, eCPXMap);
                        messageQueue.putMessage(EventType.ECPX_LOAD_RESPONSE, params);
                    } catch (Exception e) {
                        log.error("eCPXs load failure from db", e);
                        messageQueue.putMessage(EventType.ECPX_LOAD_ERROR, params);
                    }
                });
                startReloadTimeoutTimer(params);
                switchState(currentState == State.INIT ? State.WAIT_INIT_RESPONSE : State.UPDATING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleECPXResponse(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
                messageQueue.putMessage(EventType.ONE_DATA_READY);
            case UPDATING:
                cancelReloadTimeoutTimer();
                this.eCPXMap = params.get(ParamKey.ECPX_CACHE_KEY);
                log.info("ECPXs load success, size: {}", eCPXMap.size());
                startNextReloadTimer(params);
                switchState(State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleECPXError(Params params) {
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

    private void handleECPXTimeout(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
            case UPDATING:
                log.error("timeout load eCPXs");
                startNextReloadTimer(params);
                switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

}
