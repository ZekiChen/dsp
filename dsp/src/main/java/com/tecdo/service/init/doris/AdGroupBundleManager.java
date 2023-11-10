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

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tecdo.constant.ParamKey.BUNDLE_ADGROUP_DATA_CACHE_KEY;

/**
 * Created by Elwin on 2023/10/11
 */
@Slf4j
@Service
public class AdGroupBundleManager extends ServiceImpl<ReportMapper, Report> {
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
    private Map<String, BundleCost> adGroupBundleDataMap;

    @Value("${pac.timeout.load.doris.report.adgroup-bundle-data}")
    private long loadTimeout;
    @Value("${pac.interval.reload.doris.adgroup-bundle-data}")
    private long reloadInterval;

    /**
     * 从 Doris 加载 历史bundle-group的曝光、点击、花费 集合，每 5 分钟刷新一次缓存
     */
    public BundleCost getAdGroupBundleData(String bundleAndAdGroup) {
        return adGroupBundleDataMap.getOrDefault(bundleAndAdGroup, EMPTY);
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
        messageQueue.putMessage(EventType.ADGROUP_BUNDLE_DATA_LOAD, params);
    }

    private void startReloadTimeoutTimer(Params params) {
        timerId = softTimer.startTimer(EventType.ADGROUP_BUNDLE_DATA_LOAD_TIMEOUT, params, loadTimeout);
    }

    private void cancelReloadTimeoutTimer() {
        softTimer.cancel(timerId);
    }

    private void startNextReloadTimer(Params params) {
        softTimer.startTimer(EventType.ADGROUP_BUNDLE_DATA_LOAD, params, reloadInterval);
    }

    public void switchState(State state) {
        this.currentState = state;
    }

    public void handleEvent(EventType eventType, Params params) {
        switch (eventType) {
            case ADGROUP_BUNDLE_DATA_LOAD:
                handleReload(params);
                break;
            case ADGROUP_BUNDLE_DATA_LOAD_RESPONSE:
                handleResponse(params);
                break;
            case ADGROUP_BUNDLE_DATA_LOAD_ERROR:
                handleError(params);
                break;
            case ADGROUP_BUNDLE_DATA_LOAD_TIMEOUT:
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
                        long startTime = System.currentTimeMillis();
                        String startDate = DateUtil.offsetDay(new Date(), -90).toDateStr();
                        String endDate = DateUtil.today();
                        Map<String, BundleCost> adGroupBundleHistoryMap = reportMapper.listBundleAdGroupData(startDate, endDate)
                                .stream().collect(Collectors.toMap(BundleCost::toString, cost -> cost));
                        log.info("adGroup bundle data load time: {}s", (System.currentTimeMillis() - startTime) / 1000);
                        params.put(BUNDLE_ADGROUP_DATA_CACHE_KEY, adGroupBundleHistoryMap);
                        messageQueue.putMessage(EventType.ADGROUP_BUNDLE_DATA_LOAD_RESPONSE, params);
                    } catch (Exception e) {
                        log.error("adGroup bundle data load failure from db", e);
                        messageQueue.putMessage(EventType.ADGROUP_BUNDLE_DATA_LOAD_ERROR, params);
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
        cancelReloadTimeoutTimer();
        this.adGroupBundleDataMap = params.get(BUNDLE_ADGROUP_DATA_CACHE_KEY);
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
                log.info("adGroup bundle data load success, size: {}", adGroupBundleDataMap.size());
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
                log.error("timeout load bundle cost");
                startNextReloadTimer(params);
                switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

}
