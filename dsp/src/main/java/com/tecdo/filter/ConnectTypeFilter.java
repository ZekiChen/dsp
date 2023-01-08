package com.tecdo.filter;

import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Device;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.util.ConditionUtil;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 网络连接方式 过滤
 *
 * Created by Zeki on 2023/1/3
 **/
@Component
public class ConnectTypeFilter extends AbstractRecallFilter {

    private static final String CONNECT_ATTR = "connection_type";

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, List<TargetCondition> conditions) {
        TargetCondition condition = conditions.stream().filter(e -> CONNECT_ATTR.equals(e.getAttribute())).findFirst().orElse(null);
        if (condition == null) {
            return true;
        }
        Device device = bidRequest.getDevice();
        if (device == null || device.getConnectiontype() == null) {
            return false;
        }
        return ConditionUtil.compare(device.getConnectiontype().toString(), condition.getOperation(), condition.getValue());
    }
}
