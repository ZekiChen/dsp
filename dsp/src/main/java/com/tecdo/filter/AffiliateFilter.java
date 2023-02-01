package com.tecdo.filter;

import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.*;
import com.tecdo.entity.Affiliate;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.util.ConditionHelper;
import org.springframework.stereotype.Component;

/**
 * 目标渠道 过滤
 *
 * Created by Zeki on 2023/1/3
 **/
@Component
public class AffiliateFilter extends AbstractRecallFilter {

    private static final String AFF_ATTR = "affiliate";

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        TargetCondition condition = adDTO.getConditions().stream().filter(e -> AFF_ATTR.equals(e.getAttribute())).findFirst().orElse(null);
        // 该 AD 不存在需要当前 filter 处理的条件，即该 AD 对目标渠道没有要求
        if (condition == null) {
            return true;
        }
        // 该 AD 对目标渠道有要求
        return ConditionHelper.compare(affiliate.getId().toString(), condition.getOperation(), condition.getValue());
    }
}
