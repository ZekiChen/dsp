package com.tecdo.filter;

import cn.hutool.core.util.StrUtil;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Device;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.util.ConditionUtil;
import org.springframework.stereotype.Component;

/**
 * 设备OS版本号 过滤
 *
 * Created by Zeki on 2023/1/3
 **/
@Component
public class DeviceOSVFilter extends AbstractRecallFilter {

    private static final String OSV_ATTR = "device_osv";

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO) {
        TargetCondition condition = adDTO.getConditions().stream().filter(e -> OSV_ATTR.equals(e.getAttribute())).findFirst().orElse(null);
        if (condition == null) {
            return true;
        }
        Device device = bidRequest.getDevice();
        if (device == null || StrUtil.isBlank(device.getOsv())) {
            return false;
        }
        return ConditionUtil.compare(device.getGeo().getCountry(), condition.getOperation(), condition.getValue());
    }
}
