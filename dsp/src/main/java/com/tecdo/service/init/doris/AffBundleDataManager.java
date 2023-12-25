package com.tecdo.service.init.doris;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tecdo.constant.ParamKey.AFFILIATE_BUNDLE_DATA_CACHE_KEY;

/**
 * Created by Zeki on 2023/12/21
 */
@Slf4j
@Service
public class AffBundleDataManager extends ServiceImpl<ReportMapper, Report> {
    @Autowired
    private SoftTimer softTimer;
    @Autowired
    private MessageQueue messageQueue;
    @Autowired
    private ThreadPool threadPool;
    @Autowired
    private ReportMapper reportMapper;
    @Value("${pac.load.doris.affiliate-bundle-ctr.day-period:14}")
    private Integer dayPeriod;
    @Value("${pac.force-jump.pctr}")
    private Double forceJumpPCtr;

    private State currentState = State.INIT;
    private long timerId;

    /**
     * key: "{affiliateId},{bundle}"
     * value: {ctr}
     */
    private Map<String, Double> affBundleCtrMap;

    @Value("${pac.timeout.load.bundle.data}")
    private long loadTimeout;
    // 1000 * 60 * 60 * 24
    @Value("${pac.interval.reload.doris.affiliateId-bundle-ctr:86400000}")
    private long reloadInterval;
    @Value("${pac.init.reload.interval:60000}")
    private long initReloadInterval;

    private static String LOG_INFO = "affiliateId*bundle ctr";

    /**
     * 从 Doris 加载 历史affiliateId * bundle 的 ctr 集合，每 1h 刷新一次缓存
     */
    public Double getCtr(String affiliateIdBundle) {
        return affBundleCtrMap.getOrDefault(affiliateIdBundle, forceJumpPCtr);
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
        messageQueue.putMessage(EventType.AFFILIATE_BUNDLE_DATA_LOAD, params);
    }

    private void startReloadTimeoutTimer(Params params) {
        timerId = softTimer.startTimer(EventType.AFFILIATE_BUNDLE_DATA_LOAD_TIMEOUT, params, loadTimeout);
    }

    private void cancelReloadTimeoutTimer() {
        softTimer.cancel(timerId);
    }

    private void startNextReloadTimer(Params params) {
        softTimer.startTimer(EventType.AFFILIATE_BUNDLE_DATA_LOAD, params, reloadInterval);
    }

    private void startInitReloadTimer(Params params) {
        softTimer.startTimer(EventType.AFFILIATE_BUNDLE_DATA_LOAD, params, initReloadInterval);
    }

    public void switchState(State state) {
        this.currentState = state;
    }

    public void handleEvent(EventType eventType, Params params) {
        switch (eventType) {
            case AFFILIATE_BUNDLE_DATA_LOAD:
                handleReload(params);
                break;
            case AFFILIATE_BUNDLE_DATA_LOAD_RESPONSE:
                handleResponse(params);
                break;
            case AFFILIATE_BUNDLE_DATA_LOAD_ERROR:
                handleError(params);
                break;
            case AFFILIATE_BUNDLE_DATA_LOAD_TIMEOUT:
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
                        String startDate = DateUtil.offsetDay(new Date(), -dayPeriod).toDateStr();
                        String endDate = DateUtil.today();
                        Map<String, Double> map = reportMapper.listAffBundleData(startDate, endDate)
                                .stream()
                                .filter(data -> data.getCtr() != null)
                                .collect(Collectors.toMap(
                                        k -> k.getAffiliateId() + StrUtil.COMMA + k.getBundle(),
                                        Report::getCtr,
                                        (o, n) -> n)
                                );
                        log.info(LOG_INFO + " load time: {}s", (System.currentTimeMillis() - startTime) / 1000);
                        params.put(AFFILIATE_BUNDLE_DATA_CACHE_KEY, map);
                        messageQueue.putMessage(EventType.AFFILIATE_BUNDLE_DATA_LOAD_RESPONSE, params);
                    } catch (Exception e) {
                        log.error(LOG_INFO + " load failure from db", e);
                        messageQueue.putMessage(EventType.AFFILIATE_BUNDLE_DATA_LOAD_ERROR, params);
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
        this.affBundleCtrMap = params.get(AFFILIATE_BUNDLE_DATA_CACHE_KEY);
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
                log.info(LOG_INFO + " load success, size: {}", affBundleCtrMap.size());
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
                cancelReloadTimeoutTimer();
                startInitReloadTimer(params);
                switchState(State.INIT);
                break;
            case UPDATING:
                cancelReloadTimeoutTimer();
                startNextReloadTimer(params);
                switchState(State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleTimeout(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
                log.error("timeout load " + LOG_INFO);
                startInitReloadTimer(params);
                switchState(State.INIT);
                break;
            case UPDATING:
                log.error("timeout load " + LOG_INFO);
                startNextReloadTimer(params);
                switchState(State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

}
