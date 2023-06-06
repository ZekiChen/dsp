package com.tecdo.service.init.doris;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.adm.api.doris.entity.AdGroupCost;
import com.tecdo.adm.api.doris.mapper.AdGroupCostMapper;
import com.tecdo.common.util.Params;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
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

/**
 * Created by Zeki on 2022/12/27
 **/
@Slf4j
@Service
public class BudgetManager extends ServiceImpl<AdGroupCostMapper, AdGroupCost> {

    @Autowired
    private SoftTimer softTimer;
    @Autowired
    private MessageQueue messageQueue;
    @Autowired
    private ThreadPool threadPool;

    private State currentState = State.INIT;
    private long timerId;
    private boolean initFinish;

    private Map<String, Double> campaignCostMap;
    private Map<String, Double> adGroupCostMap;

    @Value("${pac.timeout.load.doris.default}")
    private long loadTimeout;
    @Value("${pac.interval.reload.doris.default}")
    private long reloadInterval;

    /**
     * 从 Doris 加载 当天campaign的实时花费 集合，每 5 秒刷新一次缓存
     */
    public Double getCampaignCost(String campaignId){
        return campaignCostMap.getOrDefault(campaignId, 0d);
    }

    /**
     * 从 Doris 加载 当天adGroup的实时花费 集合，每 5 秒刷新一次缓存
     */
    public Double getAdGroupCost(String adGroupId) {
        return this.adGroupCostMap.getOrDefault(adGroupId, 0d);
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
        messageQueue.putMessage(EventType.BUDGETS_LOAD, params);
    }

    private void startReloadTimeoutTimer(Params params) {
        timerId = softTimer.startTimer(EventType.BUDGETS_LOAD_TIMEOUT, params, loadTimeout);
    }

    private void cancelReloadTimeoutTimer() {
        softTimer.cancel(timerId);
    }

    private void startNextReloadTimer(Params params) {
        softTimer.startTimer(EventType.BUDGETS_LOAD, params, reloadInterval);
    }

    public void switchState(State state) {
        this.currentState = state;
    }

    public void handleEvent(EventType eventType, Params params) {
        switch (eventType) {
            case BUDGETS_LOAD:
                handleBudgetsReload(params);
                break;
            case BUDGETS_LOAD_RESPONSE:
                handleBudgetsResponse(params);
                break;
            case BUDGETS_LOAD_ERROR:
                handleBudgetsError(params);
                break;
            case BUDGETS_LOAD_TIMEOUT:
                handleBudgetsTimeout(params);
                break;
            default:
                log.error("Can't handle event, type: {}", eventType);
        }
    }

    private void handleBudgetsReload(Params params) {
        switch (currentState) {
            case INIT:
            case RUNNING:
                threadPool.execute(() -> {
                    try {
                        List<AdGroupCost> costList = list(Wrappers.<AdGroupCost>lambdaQuery()
                                .eq(AdGroupCost::getCreateDate, DateUtil.today()));
                        Map<String, Double> campaignBudgetMap = //
                          costList.stream()
                                  .collect(Collectors.groupingBy(AdGroupCost::getCampaignId,
                                                                 Collectors.summingDouble(
                                                                   AdGroupCost::getSumSuccessPrice)));
                        Map<String, Double> adGroupBudgetMap = costList.stream().collect(Collectors.toMap(
                                AdGroupCost::getAdGroupId, AdGroupCost::getSumSuccessPrice, (o,n)->n));
                        params.put(ParamKey.CAMPAIGN_BUDGETS_CACHE_KEY, campaignBudgetMap);
                        params.put(ParamKey.AD_GROUP_BUDGETS_CACHE_KEY, adGroupBudgetMap);
                        messageQueue.putMessage(EventType.BUDGETS_LOAD_RESPONSE, params);
                    } catch (Exception e) {
                        log.error("budgets load failure from db", e);
                        messageQueue.putMessage(EventType.BUDGETS_LOAD_ERROR, params);
                    }
                });
                startReloadTimeoutTimer(params);
                switchState(currentState == State.INIT ? State.WAIT_INIT_RESPONSE : State.UPDATING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleBudgetsResponse(Params params) {
        cancelReloadTimeoutTimer();
        this.campaignCostMap = params.get(ParamKey.CAMPAIGN_BUDGETS_CACHE_KEY);
        this.adGroupCostMap = params.get(ParamKey.AD_GROUP_BUDGETS_CACHE_KEY);
        log.info("budgets load success, campaign size: {}, ad group size: {}",
                 campaignCostMap.size(),
                 adGroupCostMap.size());
        if (!initFinish) {
            messageQueue.putMessage(EventType.ONE_DATA_READY);
            initFinish = true;
        }
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
            case UPDATING:
                startNextReloadTimer(params);
                switchState(State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

    private void handleBudgetsError(Params params) {
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

    private void handleBudgetsTimeout(Params params) {
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
            case UPDATING:
                log.error("timeout load budgets");
                startNextReloadTimer(params);
                switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }
}
