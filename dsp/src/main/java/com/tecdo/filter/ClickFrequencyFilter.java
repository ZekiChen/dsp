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

  private static final String CLICK_FREQUENCY = ConditionEnum.CLICK_FREQUENCY.getDesc();
  private static final String CLICK_FREQUENCY_HOUR = ConditionEnum.CLICK_FREQUENCY_HOUR.getDesc();

  private final CacheService cacheService;

  @Override
  public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
    TargetCondition conditionToday = adDTO.getConditionMap().get(CLICK_FREQUENCY);
    TargetCondition conditionByhour = adDTO.getConditionMap().get(CLICK_FREQUENCY_HOUR);
    boolean isPassedToday = true, isPassedByHour = true;

    Integer campaignId = adDTO.getCampaign().getId();
    String deviceId = bidRequest.getDevice().getIfa();

    if (conditionToday != null) {
      Integer countToday = cacheService.getFrequencyCache().getClickCountToday(campaignId.toString(), deviceId);
      isPassedToday = ConditionHelper.compare(String.valueOf(countToday),
              conditionToday.getOperation(),
              conditionToday.getValue());
    }

    if (conditionByhour != null) {
      Integer countByHour = cacheService.getFrequencyCache().getClickCountByHour(campaignId.toString(), deviceId);
      isPassedByHour = ConditionHelper.compare(String.valueOf(countByHour),
              conditionByhour.getOperation(),
              conditionByhour.getValue());
    }

    return isPassedToday && isPassedByHour;
  }
}
