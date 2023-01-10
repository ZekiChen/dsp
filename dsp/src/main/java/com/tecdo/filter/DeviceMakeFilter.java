package com.tecdo.filter;

import cn.hutool.core.util.StrUtil;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.util.ConditionUtil;
import org.springframework.stereotype.Component;

/**
 * 设备品牌/制造商 过滤
 *
 * Created by Zeki on 2023/1/3
 **/
@Component
public class DeviceMakeFilter extends AbstractRecallFilter {

    private static final String MAKE_ATTR = "device_make";

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO) {
        TargetCondition condition = adDTO.getConditions().stream().filter(e -> MAKE_ATTR.equals(e.getAttribute())).findFirst().orElse(null);
        if (condition == null) {
            return true;
        }
        if (StrUtil.isBlank(bidRequest.getDevice().getMake())) {
            return false;
        }
        return ConditionUtil.compare(bidRequest.getDevice().getMake(), condition.getOperation(), condition.getValue());
    }
}
