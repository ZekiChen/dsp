package com.tecdo.service.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.mapper.AffiliateMapper;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Zeki on 2022/12/27
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateManager extends ServiceImpl<AffiliateMapper, Affiliate> {

    private final SoftTimer softTimer;
    private final MessageQueue messageQueue;
    private final ThreadPool threadPool;

    private State currentState = State.INIT;
    private long timerId;

    private Map<String, Affiliate> affiliateMap;
    private Map<Integer, Affiliate> affiliateIdMap;

    @Value("${pac.timeout.load.db.default}")
    private long loadTimeout;
    @Value("${pac.interval.reload.db.default}")
    private long reloadInterval;

    private static final Affiliate EMPTY = new Affiliate();

    /**
     * 从 DB 加载 affiliate 集合，每 5 分钟刷新一次缓存
     */
    public Map<String, Affiliate> getAffiliateMap() {
        return this.affiliateMap;
    }

    public Affiliate getAffiliate(String secret){
        return affiliateMap.get(secret);
    }

    public Affiliate getAffiliate(Integer id) {
        return affiliateIdMap.getOrDefault(id, EMPTY);
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
        messageQueue.putMessage(EventType.AFFILIATES_LOAD, params);
    }

    private void startReloadTimeoutTimer(Params params) {
        timerId = softTimer.startTimer(EventType.AFFILIATES_LOAD_TIMEOUT, params, loadTimeout);
    }

    private void cancelReloadTimeoutTimer() {
        softTimer.cancel(timerId);
    }

    private void startNextReloadTimer(Params params) {
        softTimer.startTimer(EventType.AFFILIATES_LOAD, params, reloadInterval);
    }

    public void switchState(State state) {
        this.currentState = state;
    }

    public void handleEvent(EventType eventType, Params params) {
        switch (eventType) {
            case AFFILIATES_LOAD:
                handleAffiliatesReload(params);
                break;
            case AFFILIATES_LOAD_RESPONSE:
                handleAffiliatesResponse(params);
                break;
            case AFFILIATES_LOAD_ERROR:
                handleAffiliatesError(params);
                break;
            case AFFILIATES_LOAD_TIMEOUT:
                handleAffiliatesTimeout(params);
                break;
            default:
                log.error("Can't handle event, type: {}", eventType);
        }
    }

    private void handleAffiliatesReload(Params params) {
        switch (currentState) {
            case INIT:
            case RUNNING:
                threadPool.execute(() -> {
                    try {
                        LambdaQueryWrapper<Affiliate> wrapper =
                                Wrappers.<Affiliate>lambdaQuery().eq(Affiliate::getStatus, 1);
                        List<Affiliate> affiliateList = list(wrapper);
                        Map<String, Affiliate> affiliateMap = new HashMap<>();
                        Map<Integer, Affiliate> affiliateIdMap = new HashMap<>();
                        for (Affiliate affiliate : affiliateList) {
                            String[] split = affiliate.getSecret().split(",");
                            for (String s : split) {
                                affiliateMap.put(s, affiliate);
                            }
                            affiliateIdMap.put(affiliate.getId(), affiliate);
                        }
                        params.put(ParamKey.AFFILIATES_CACHE_KEY, affiliateMap);
                        params.put(ParamKey.AFFILIATE_ID_CACHE_KEY, affiliateIdMap);
                        messageQueue.putMessage(EventType.AFFILIATES_LOAD_RESPONSE, params);
                    } catch (Exception e) {
                        log.error("affiliates load failure from db", e);
                        messageQueue.putMessage(EventType.AFFILIATES_LOAD_ERROR, params);
                    }
                });
                startReloadTimeoutTimer(params);
                switchState(currentState == State.INIT ? State.WAIT_INIT_RESPONSE : State.UPDATING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleAffiliatesResponse(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
                messageQueue.putMessage(EventType.ONE_DATA_READY);
            case UPDATING:
                cancelReloadTimeoutTimer();
                this.affiliateMap = params.get(ParamKey.AFFILIATES_CACHE_KEY);
                this.affiliateIdMap = params.get(ParamKey.AFFILIATE_ID_CACHE_KEY);
                log.info("affiliates load success, size: {}", affiliateMap.size());
                startNextReloadTimer(params);
                switchState(State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleAffiliatesError(Params params) {
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

    private void handleAffiliatesTimeout(Params params) {
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

}
