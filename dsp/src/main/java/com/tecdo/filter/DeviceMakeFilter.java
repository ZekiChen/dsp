package com.tecdo.filter;

import cn.hutool.core.util.StrUtil;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.filter.util.ConditionHelper;
import org.springframework.stereotype.Component;

/**
 * 设备品牌/制造商 过滤
 *
 * Created by Zeki on 2023/1/3
 **/
@Component
public class DeviceMakeFilter extends AbstractRecallFilter {

    private static final String MAKE_ATTR = ConditionEnum.DEVICE_MAKE.getDesc();

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        TargetCondition condition = adDTO.getConditions().stream().filter(e -> MAKE_ATTR.equals(e.getAttribute())).findFirst().orElse(null);
        if (condition == null) {
            return true;
        }
        if (StrUtil.isBlank(bidRequest.getDevice().getMake())) {
            return false;
        }
        return ConditionHelper.compare(bidRequest.getDevice().getMake(), condition.getOperation(), condition.getValue());
    }
}
