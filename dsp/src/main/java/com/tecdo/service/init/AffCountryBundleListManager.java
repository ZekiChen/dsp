package com.tecdo.service.init;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.AffCountryBundleList;
import com.tecdo.adm.api.delivery.mapper.AffCountryBundleListMapper;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Zeki on 2023/5/15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AffCountryBundleListManager extends ServiceImpl<AffCountryBundleListMapper, AffCountryBundleList> {

    private final SoftTimer softTimer;
    private final MessageQueue messageQueue;
    private final ThreadPool threadPool;

    private State currentState = State.INIT;
    private long timerId;

    private Map<Integer, List<AffCountryBundleList>> affCountryBundleListMap;

    @Value("${pac.timeout.load.db.default}")
    private long loadTimeout;
    @Value("${pac.interval.reload.db.default}")
    private long reloadInterval;

    /**
     * 从 DB 加载 affiliate 集合，每 5 分钟刷新一次缓存
     */
    public Map<Integer, List<AffCountryBundleList>> getAffCountryBundleListMap() {
        return this.affCountryBundleListMap;
    }

    public List<AffCountryBundleList> listAffCountryBundleList(Integer affiliateId) {
        return affCountryBundleListMap.get(affiliateId);
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
        messageQueue.putMessage(EventType.AFF_COUNTRY_BUNDLE_LIST_LOAD, params);
    }

    private void startReloadTimeoutTimer(Params params) {
        timerId = softTimer.startTimer(EventType.AFF_COUNTRY_BUNDLE_LIST_LOAD_TIMEOUT, params, loadTimeout);
    }

    private void cancelReloadTimeoutTimer() {
        softTimer.cancel(timerId);
    }

    private void startNextReloadTimer(Params params) {
        softTimer.startTimer(EventType.AFF_COUNTRY_BUNDLE_LIST_LOAD, params, reloadInterval);
    }

    public void switchState(State state) {
        this.currentState = state;
    }

    public void handleEvent(EventType eventType, Params params) {
        switch (eventType) {
            case AFF_COUNTRY_BUNDLE_LIST_LOAD:
                handleReload(params);
                break;
            case AFF_COUNTRY_BUNDLE_LIST_LOAD_RESPONSE:
                handleResponse(params);
                break;
            case AFF_COUNTRY_BUNDLE_LIST_LOAD_ERROR:
                handleError(params);
                break;
            case AFF_COUNTRY_BUNDLE_LIST_LOAD_TIMEOUT:
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
                        Map<Integer, List<AffCountryBundleList>> affCountryBundleListMap =
                                list().stream().collect(Collectors.groupingBy(AffCountryBundleList::getAffiliateId));
                        params.put(ParamKey.AFF_COUNTRY_BUNDLE_LIST_CACHE_KEY, affCountryBundleListMap);
                        messageQueue.putMessage(EventType.AFF_COUNTRY_BUNDLE_LIST_LOAD_RESPONSE, params);
                    } catch (Exception e) {
                        log.error("aff country bundle list load failure from db", e);
                        messageQueue.putMessage(EventType.AFF_COUNTRY_BUNDLE_LIST_LOAD_ERROR, params);
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
                this.affCountryBundleListMap = params.get(ParamKey.AFF_COUNTRY_BUNDLE_LIST_CACHE_KEY);
                log.info("aff country bundle list load success, size: {}", affCountryBundleListMap.size());
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
                log.error("timeout load aff country bundle");
                startNextReloadTimer(params);
                switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

}
