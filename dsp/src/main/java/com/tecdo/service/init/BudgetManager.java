package com.tecdo.service.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tecdo.common.Params;
import com.tecdo.common.ThreadPool;
import com.tecdo.constant.Constant;
import com.tecdo.constant.EventType;
import com.tecdo.constant.ParamKey;
import com.tecdo.controller.MessageQueue;
import com.tecdo.controller.SoftTimer;
import com.tecdo.entity.doris.AdGroupCost;
import com.tecdo.mapper.doris.AdGroupCostMapper;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Zeki on 2022/12/27
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetManager extends ServiceImpl<AdGroupCostMapper, AdGroupCost> {

    private final SoftTimer softTimer;
    private final MessageQueue messageQueue;

    private State currentState = State.INIT;
    private long timerId;

    private Map<String, Double> campaignBudgetMap;
    private Map<String, Double> adGroupBudgetMap;

    /**
     * 从 Doris 加载 当天campaign的实时花费 集合，每 5 秒刷新一次缓存
     */
    public Double getCampaignBudget(String campaignId, Double defaultValue){
        return campaignBudgetMap.getOrDefault(campaignId, defaultValue);
    }

    /**
     * 从 Doris 加载 当天adGroup的实时花费 集合，每 5 秒刷新一次缓存
     */
    public Double getAdGroupBudget(String adGroupId, Double defaultValue) {
        return this.adGroupBudgetMap.getOrDefault(adGroupId, defaultValue);
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
        timerId = softTimer.startTimer(EventType.BUDGETS_LOAD_TIMEOUT, params, Constant.TIMEOUT_LOAD_DB_CACHE_GENERAL);
    }

    private void cancelReloadTimeoutTimer() {
        softTimer.cancel(timerId);
    }

    private void startNextReloadTimer(Params params) {
        softTimer.startTimer(EventType.BUDGETS_LOAD, params, Constant.INTERVAL_RELOAD_BUDGET_CACHE);
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
                ThreadPool.getInstance().execute(() -> {
                    try {
                        String today = DateUtil.today();
                        LambdaQueryWrapper<AdGroupCost> wrapper = Wrappers.<AdGroupCost>lambdaQuery()
                                .ge(AdGroupCost::getCreateDate, today + " 00:00:00")
                                .lt(AdGroupCost::getCreateDate, today + " 23:59:59");
                        List<AdGroupCost> costList = list(wrapper);
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
        this.campaignBudgetMap = params.get(ParamKey.CAMPAIGN_BUDGETS_CACHE_KEY);
        this.adGroupBudgetMap = params.get(ParamKey.AD_GROUP_BUDGETS_CACHE_KEY);
        switch (currentState) {
            case WAIT_INIT_RESPONSE:
                log.info("budgets load success, campaign size: {}, ad group size: {}",
                         campaignBudgetMap.size(),
                         adGroupBudgetMap.size());
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
                startNextReloadTimer(params);
                switchState(currentState == State.WAIT_INIT_RESPONSE ? State.INIT : State.RUNNING);
                break;
            default:
                log.error("Can't handle event, state: {}", currentState);
        }
    }

}