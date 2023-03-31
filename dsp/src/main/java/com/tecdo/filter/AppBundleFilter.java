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
 * Bundle过滤（仅App）
 *
 * Created by Zeki on 2023/1/3
 **/
@Component
public class AppBundleFilter extends AbstractRecallFilter {

    private static final String BUNDLE_ATTR = ConditionEnum.BUNDLE.getDesc();

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        TargetCondition condition = adDTO.getConditions().stream().filter(e -> BUNDLE_ATTR.equals(e.getAttribute())).findFirst().orElse(null);
        if (condition == null) {
            return true;
        }
        if (StrUtil.isBlank(bidRequest.getApp().getBundle())) {
            return false;
        }
        return ConditionHelper.compare(bidRequest.getApp().getBundle(), condition.getOperation(), condition.getValue());
    }
}
