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
public class ImpFrequencyFilter extends AbstractRecallFilter {

  private static final String IMP_FREQUENCY_ATTR = "imp_frequency";

  private final CacheService cacheService;

  @Override
  public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
    TargetCondition condition = adDTO.getConditions()
                                     .stream()
                                     .filter(e -> IMP_FREQUENCY_ATTR.equals(e.getAttribute()))
                                     .findFirst()
                                     .orElse(null);
    if (condition == null) {
      return true;
    }
    Integer campaignId = adDTO.getCampaign().getId();
    String deviceId = Optional.ofNullable(bidRequest.getDevice()).map(Device::getIfa).orElse(null);
    Integer countToday = cacheService.getImpCountToday(campaignId.toString(), deviceId);

    return ConditionHelper.compare(String.valueOf(countToday),
                                   condition.getOperation(),
                                   condition.getValue());
  }
}
