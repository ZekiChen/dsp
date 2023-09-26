package com.tecdo.filter;

import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.filter.util.ConditionHelper;
import org.springframework.stereotype.Component;

/**
 * 插屏广告过滤
 *
 * Created by Zeki on 2023/9/25
 */
@Component
public class InterstitialFilter extends AbstractRecallFilter {

    private static final String ATTRIBUTE = ConditionEnum.INTERSTITIAL.getDesc();

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        TargetCondition condition = adDTO.getConditionMap().get(ATTRIBUTE);
        if (condition == null) {
            return true;
        }
        Integer instl = imp.getInstl();
        return ConditionHelper.compare(instl.toString(), condition.getOperation(), condition.getValue());
    }
}
