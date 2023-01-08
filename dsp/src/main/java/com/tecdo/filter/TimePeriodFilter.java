package com.tecdo.filter;

import cn.hutool.core.date.DateUtil;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.util.ConditionUtil;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 投放时间段 过滤
 *
 * Created by Zeki on 2023/1/3
 **/
@Component
public class TimePeriodFilter extends AbstractRecallFilter {

    private static final String HOUR_ATTR = "hour";

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, List<TargetCondition> conditions) {
        TargetCondition condition = conditions.stream().filter(e -> HOUR_ATTR.equals(e.getAttribute())).findFirst().orElse(null);
        if (condition == null) {
            return true;
        }
        int curHour = DateUtil.thisHour(true);
        return ConditionUtil.compare(String.valueOf(curHour), condition.getOperation(), condition.getValue());
    }
}
