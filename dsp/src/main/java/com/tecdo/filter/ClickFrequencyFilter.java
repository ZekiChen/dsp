package com.tecdo.filter;

import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Device;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.Affiliate;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.util.ConditionHelper;

import com.tecdo.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClickFrequencyFilter extends AbstractRecallFilter {

  private static final String CLICK_FREQUENCY_ATTR = "click_frequency";

  private final CacheService cacheService;

  @Override
  public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
    TargetCondition condition = adDTO.getConditions()
                                     .stream()
                                     .filter(e -> CLICK_FREQUENCY_ATTR.equals(e.getAttribute()))
                                     .findFirst()
                                     .orElse(null);
    if (condition == null) {
      return true;
    }
    Integer campaignId = adDTO.getCampaign().getId();
    String deviceId = bidRequest.getDevice().getIfa();
    Integer countToday = cacheService.getClickCountToday(campaignId.toString(), deviceId);

    return ConditionHelper.compare(String.valueOf(countToday),
                                   condition.getOperation(),
                                   condition.getValue());
  }
}
