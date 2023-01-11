package com.tecdo.fsm.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.tecdo.common.Params;
import com.tecdo.constant.EventType;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.AbstractRecallFilter;
import com.tecdo.filter.util.FilterChainUtil;
import com.tecdo.filter.factory.RecallFiltersFactory;
import com.tecdo.fsm.task.state.ITaskState;
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

    private AdManager adManager;
    private RtaInfoManager rtaInfoManager;
    private RecallFiltersFactory filtersFactory;

    private ITaskState currentState;

    public void init() {

    }

    public void reset() {

    }

    public void switchState(ITaskState newState) {
        this.currentState = newState;
    }

    public void startTimer(long delay) {

    }

    public void cancelTimer() {

    }

    public void handleEvent(EventType eventType, Params params) {
        currentState.handleEvent(eventType, params, this);
    }

    /**
     * 广告召回（在此之前，应该有一个参数校验器，对 BidRequest 进行参数校验，不满足条件就直接不参与本次竞价）
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
