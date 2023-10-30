package com.tecdo.filter;

import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Created by Zeki on 2023/10/23
 */
@Component
@RequiredArgsConstructor
public class AutoBundleFilter extends AbstractRecallFilter {

    private static final String AUTO_BUNDLE = ConditionEnum.AUTO_BUNDLE.getDesc();
    private static final String AUTO_BUNDLE_EXCEPT = ConditionEnum.AUTO_BUNDLE_EXCEPT.getDesc();

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        TargetCondition autoBundleCond = adDTO.getConditionMap().get(AUTO_BUNDLE);
        TargetCondition autoBundleExceptCond = adDTO.getConditionMap().get(AUTO_BUNDLE_EXCEPT);
        String bundleId = bidRequest.getApp().getBundle();

        // (白名单不为空 && 白名单包含bundle) || (黑名单为空 || 黑名单不包含bundle)
        return (autoBundleExceptCond != null && autoBundleExceptCond.getValue().contains(bundleId)) ||
                (autoBundleCond == null || !autoBundleCond.getValue().contains(bundleId));
    }
}
