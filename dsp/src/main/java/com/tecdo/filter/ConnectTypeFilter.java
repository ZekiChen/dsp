package com.tecdo.filter;

import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.Affiliate;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.util.ConditionHelper;
import org.springframework.stereotype.Component;

/**
 * 网络连接方式 过滤
 *
 * Created by Zeki on 2023/1/3
 **/
@Component
public class ConnectTypeFilter extends AbstractRecallFilter {

    private static final String CONNECT_ATTR = "connection_type";

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        TargetCondition condition = adDTO.getConditions().stream().filter(e -> CONNECT_ATTR.equals(e.getAttribute())).findFirst().orElse(null);
        if (condition == null) {
            return true;
        }

        if (bidRequest.getDevice().getConnectiontype() == null) {
            return false;
        }
        return ConditionHelper.compare(bidRequest.getDevice().getConnectiontype().toString(), condition.getOperation(), condition.getValue());
    }
}
