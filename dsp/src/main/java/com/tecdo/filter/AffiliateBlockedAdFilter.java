package com.tecdo.filter;

import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AffiliateBlockedAdFilter extends AbstractRecallFilter {

    private static final String ATTRIBUTE = ConditionEnum.AFFILIATE_BLOCKED_AD.getDesc();

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        TargetCondition condition = adDTO.getConditionMap().get(ATTRIBUTE);
        // 目前默认不开启这个过滤
        if (condition == null) {
            return true;
        }
        List<String> badv = bidRequest.getBadv();
        if (CollectionUtils.isNotEmpty(badv) && badv.contains(adDTO.getCampaign().getDomain())) {
            return false;
        }
        List<String> bapp = bidRequest.getBapp();
        if (CollectionUtils.isNotEmpty(bapp) &&
            bapp.contains(adDTO.getCampaign().getPackageName())) {
            return false;
        }

        return true;

    }
}
