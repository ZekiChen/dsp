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
 * 国家 过滤
 *
 * Created by Zeki on 2023/1/3
 **/
@Component
public class DeviceCountryFilter extends AbstractRecallFilter {

    private static final String COUNTRY_ATTR = "device_country";

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, List<TargetCondition> conditions) {
        TargetCondition condition = conditions.stream().filter(e -> COUNTRY_ATTR.equals(e.getAttribute())).findFirst().orElse(null);
        if (condition == null) {
            return true;
        }
        Device device = bidRequest.getDevice();
        if (device == null || device.getGeo() == null || StrUtil.isBlank(device.getGeo().getCountry())) {
            return false;
        }
        return ConditionUtil.compare(device.getGeo().getCountry(), condition.getOperation(), condition.getValue());
    }
}
