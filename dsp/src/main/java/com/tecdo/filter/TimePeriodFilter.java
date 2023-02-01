package com.tecdo.filter;

import cn.hutool.core.date.DateUtil;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.Affiliate;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.util.ConditionHelper;
import org.springframework.stereotype.Component;

/**
 * 投放时间段 过滤
 *
 * Created by Zeki on 2023/1/3
 **/
@Component
public class TimePeriodFilter extends AbstractRecallFilter {

    private static final String HOUR_ATTR = "hour";

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        TargetCondition condition = adDTO.getConditions().stream().filter(e -> HOUR_ATTR.equals(e.getAttribute())).findFirst().orElse(null);
        if (condition == null) {
            return true;
        }
        int curHour = DateUtil.thisHour(true);
        return ConditionHelper.compare(String.valueOf(curHour), condition.getOperation(), condition.getValue());
    }
}
