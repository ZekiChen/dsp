package com.tecdo.filter;

import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.filter.util.ConditionHelper;

import com.tecdo.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClickFrequencyFilter extends AbstractRecallFilter {

  private static final String ATTRIBUTE = ConditionEnum.CLICK_FREQUENCY.getDesc();

  private final CacheService cacheService;

  @Override
  public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
    TargetCondition condition = adDTO.getConditionMap().get(ATTRIBUTE);
    if (condition == null) {
      return true;
    }
    Integer campaignId = adDTO.getCampaign().getId();
    String deviceId = bidRequest.getDevice().getIfa();
    Integer countToday = cacheService.getFrequencyCache().getClickCountToday(campaignId.toString(), deviceId);

    return ConditionHelper.compare(String.valueOf(countToday),
                                   condition.getOperation(),
                                   condition.getValue());
  }
}
