package com.tecdo.service.init.doris;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.doris.dto.BundleCost;
import com.tecdo.adm.api.doris.entity.Report;
import com.tecdo.adm.api.doris.mapper.ReportMapper;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.core.launch.thread.ThreadPool;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tecdo.constant.ParamKey.BUNDLE_COST_CACHE_KEY;

/**
 * Created by Elwin on 2023/10/11
 */
@Slf4j
@Service
public class BundleCostManager extends ServiceImpl<ReportMapper, Report> {
    @Autowired
    private SoftTimer softTimer;
    @Autowired
    private MessageQueue messageQueue;
    @Autowired
    private ThreadPool threadPool;
    @Autowired
    private ReportMapper reportMapper;

    private State currentState = State.INIT;
    private long timerId;
    private static final BundleCost EMPTY = new BundleCost();

    /**
     * Key: "${bundleId}, ${adGroupId}"
     */
    private Map<String, BundleCost> bundleCostMap;

    @Value("${pac.timeout.load.doris.report.bundle-cap}")
    private long loadTimeout;
    @Value("${pac.interval.reload.doris.report.bundle-cap}")
    private long reloadInterval;

    /**
     * 从 Doris 加载 当天bundle-group的曝光、点击、花费 集合，每 5 分钟刷新一次缓存
     */
    public BundleCost getBundleCost(String bundleAndAdGroup) {
        return bundleCostMap.getOrDefault(bundleAndAdGroup, EMPTY);
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
        messageQueue.putMessage(EventType.BUNDLE_COST_LOAD, params);
    }

    private void startReloadTimeoutTimer(Params params) {
        timerId = softTimer.startTimer(EventType.BUNDLE_COST_LOAD_TIMEOUT, params, loadTimeout);
    }

    private void cancelReloadTimeoutTimer() {
        softTimer.cancel(timerId);
    }

    private void startNextReloadTimer(Params params) {
        softTimer.startTimer(EventType.BUNDLE_COST_LOAD, params, reloadInterval);
    }

    public void switchState(State state) {
        this.currentState = state;
    }

    public void handleEvent(EventType eventType, Params params) {
        switch (eventType) {
            case BUNDLE_COST_LOAD:
                handleBundleCostReload(params);
                break;
            case BUNDLE_COST_LOAD_RESPONSE:
                handleBundleCostResponse(params);
                break;
            case BUNDLE_COST_LOAD_ERROR:
                handleBundleCostError(params);
                break;
            case BUNDLE_COST_LOAD_TIMEOUT:
                handleBundleCostTimeout(params);
                break;
            default:
                log.error("Can't handle event, type: {}", eventType);
        }
    }

    private void handleBundleCostReload(Params params) {
        switch (currentState) {
            case INIT:
            case RUNNING:
                threadPool.execute(() -> {
                    try {
                        long startTime = System.currentTimeMillis();
                        List<BundleCost> costList = reportMapper.getBundleCostByDay(DateUtil.today());
                        Map<String, BundleCost> bundleCostMap = costList.stream().collect(Collectors.toMap(BundleCost::toString, cost -> cost));
                        log.info("bundle cap load time: {}s", (System.currentTimeMillis() - startTime) / 1000);
                        params.put(BUNDLE_COST_CACHE_KEY, bundleCostMap);
                        messageQueue.putMessage(EventType.BUNDLE_COST_LOAD_RESPONSE, params);
                    } catch (Exception e) {
                        log.error("bundle cost load failure from db", e);
                        messageQueue.putMessage(EventType.BUNDLE_COST_LOAD_ERROR, params);
                    }
                });
                startReloadTimeoutTimer(params);
                switchState(currentState == State.INIT ? State.WAIT_INIT_RESPONSE : State.UPDATING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleBundleCostResponse(Params params) {
        cancelReloadTimeoutTimer();
        this.bundleCostMap = params.get(BUNDLE_COST_CACHE_KEY);
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
                log.info("bundle cost load success, bundle cost size: {}",
                        bundleCostMap.size());
                messageQueue.putMessage(EventType.ONE_DATA_READY);
                startNextReloadTimer(params);
                switchState(State.RUNNING);
                break;
            case UPDATING:
                startNextReloadTimer(params);
                switchState(State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleBundleCostError(Params params) {
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

    private void handleBundleCostTimeout(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
            case UPDATING:
                log.error("timeout load bundle cost");
                startNextReloadTimer(params);
                switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

}
