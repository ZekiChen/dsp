package com.tecdo.fsm.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.controller.SoftTimer;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.AbstractRecallFilter;
import com.tecdo.filter.util.FilterChainUtil;
import com.tecdo.filter.factory.RecallFiltersFactory;
import com.tecdo.fsm.task.state.ITaskState;
import com.tecdo.fsm.task.state.InitState;
import com.tecdo.service.init.AdManager;
import com.tecdo.service.init.RtaInfoManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class Task {

    private Imp imp;
    private BidRequest bidRequest;

    private final AdManager adManager = SpringUtil.getBean(AdManager.class);
    private final RtaInfoManager rtaInfoManager = SpringUtil.getBean(RtaInfoManager.class);
    private final RecallFiltersFactory filtersFactory = SpringUtil.getBean(RecallFiltersFactory.class);
    private final SoftTimer softTimer = SpringUtil.getBean(SoftTimer.class);
    private final Map<EventType, Long> eventTimerMap = new HashMap<>();

    private ITaskState currentState = SpringUtil.getBean(InitState.class);

    public void init(BidRequest bidRequest, Imp imp) {
        this.bidRequest = bidRequest;
        this.imp = imp;
    }

    public void reset() {

    }

    public void switchState(ITaskState newState) {
        this.currentState = newState;
    }

    public void startTimer(EventType eventType, Params params, long delay) {
        long timerId = softTimer.startTimer(eventType, params, delay);
        eventTimerMap.put(eventType, timerId);
    }

    public void cancelTimer(EventType eventType) {
        if (eventTimerMap.containsKey(eventType)) {
            softTimer.cancel(eventTimerMap.get(eventType));
        } else {
            log.warn("context: {}, not exist this timer: {}", bidRequest.getId(), eventType);
        }
    }

    public void handleEvent(EventType eventType, Params params) {
        currentState.handleEvent(eventType, params, this);
    }

    /**
     * 广告召回
     */
    public Map<Integer, AdDTO> listRecallAd() {
        List<AbstractRecallFilter> filters = filtersFactory.createFilters();
        FilterChainUtil.assemble(filters);

        Map<Integer, AdDTO> resMap = new HashMap<>();
        for (AdDTO adDTO : adManager.getAdDTOMap().values()) {
            List<TargetCondition> conditions = listLegalCondition(adDTO.getConditions());
            adDTO.setConditions(conditions);
            // 该 AD 没有定投需求，说明投哪都行，召回
            if (CollUtil.isEmpty(conditions)) {
                log.info("ad: {} doesn't have condition, direct recall", adDTO.getAd().getId());
                resMap.put(adDTO.getAd().getId(), adDTO);
                continue;
            }
            // 有定投需求，校验：每个 AD 都需要被所有 filter 判断一遍
            if (executeFilter(filters.get(0), adDTO)) {
                resMap.put(adDTO.getAd().getId(), adDTO);
            }
        }
        return resMap;
    }

    /**
     * 获取参数合法的condition对象（即非空校验）
     */
    private List<TargetCondition> listLegalCondition(List<TargetCondition> conditions) {
        return conditions.stream().filter(e -> e != null
                && StrUtil.isAllNotBlank(e.getAttribute(), e.getOperation(), e.getValue())).collect(Collectors.toList());
    }

    /**
     * 每个 AD 都需要被所有 filter 判断一遍
     */
    private boolean executeFilter(AbstractRecallFilter curFilter, AdDTO adDTO) {
        boolean filterFlag = curFilter.doFilter(bidRequest, imp, adDTO);
        while (filterFlag && curFilter.hasNext()) {
            curFilter = curFilter.getNextFilter();
            filterFlag = curFilter.doFilter(bidRequest, imp, adDTO);
        }
        return filterFlag;
    }
}
