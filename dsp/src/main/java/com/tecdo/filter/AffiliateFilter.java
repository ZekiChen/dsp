package com.tecdo.filter;

import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.openrtb.request.*;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.filter.util.ConditionHelper;
import org.springframework.stereotype.Component;

/**
 * 目标渠道 过滤
 *
 * Created by Zeki on 2023/1/3
 **/
@Component
public class AffiliateFilter extends AbstractRecallFilter {

    private static final String ATTRIBUTE = ConditionEnum.AFFILIATE.getDesc();

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTOWrapper adDTOWrapper, Affiliate affiliate) {
        AdDTO adDTO = adDTOWrapper.getAdDTO();
        TargetCondition condition = adDTO.getConditionMap().get(ATTRIBUTE);
        // 该 AD 不存在需要当前 filter 处理的条件，即该 AD 对目标渠道没有要求
        if (condition == null) {
            return true;
        }
        // 该 AD 对目标渠道有要求
        return ConditionHelper.compare(affiliate.getId().toString(), condition.getOperation(), condition.getValue());
    }
}
