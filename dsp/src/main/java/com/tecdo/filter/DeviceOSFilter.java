package com.tecdo.filter;

import cn.hutool.core.util.StrUtil;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Device;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.util.ConditionUtil;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 设备OS 过滤
 *
 * Created by Zeki on 2023/1/3
 **/
@Component
public class DeviceOSFilter extends AbstractRecallFilter {

    private static final String OS_ATTR = "device_os";

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, List<TargetCondition> conditions) {
        TargetCondition condition = conditions.stream().filter(e -> OS_ATTR.equals(e.getAttribute())).findFirst().orElse(null);
        if (condition == null) {
            return true;
        }
        Device device = bidRequest.getDevice();
        if (device == null || StrUtil.isBlank(device.getOs())) {
            return false;
        }
        return ConditionUtil.compare(device.getGeo().getCountry(), condition.getOperation(), condition.getValue());
    }
}
