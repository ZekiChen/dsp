package com.tecdo.filter;

import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.filter.util.ConditionHelper;
import com.tecdo.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImpFrequencyFilter extends AbstractRecallFilter {

  private static final String IMP_FREQUENCY = ConditionEnum.IMP_FREQUENCY.getDesc();
  private static final String IMP_FREQUENCY_HOUR = ConditionEnum.IMP_FREQUENCY_HOUR.getDesc();

  private final CacheService cacheService;

  @Value("${pac.recall.filter.imp-frequency.enabled}")
  private boolean filterEnabled;

  @Override
  public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
    TargetCondition conditionToday = adDTO.getConditionMap().get(IMP_FREQUENCY);
    TargetCondition conditionByHour = adDTO.getConditionMap().get(IMP_FREQUENCY_HOUR);
    boolean isPassedToday = true, isPassedByHour = true;

    if (!filterEnabled) {
      return true;
    }

    Integer campaignId = adDTO.getCampaign().getId();
    String deviceId = bidRequest.getDevice().getIfa();

    if (conditionToday != null) {
      Integer countToday = cacheService.getFrequencyCache().getImpCountToday(campaignId.toString(), deviceId);
      isPassedToday = ConditionHelper.compare(String.valueOf(countToday),
              conditionToday.getOperation(),
              conditionToday.getValue());
    }
    if (conditionByHour != null) {
      Integer countByHour = cacheService.getFrequencyCache().getImpCountByHour(campaignId.toString(), deviceId);
      isPassedByHour = ConditionHelper.compare(String.valueOf(countByHour),
              conditionByHour.getOperation(),
              conditionByHour.getValue());
    }

    return isPassedByHour && isPassedToday;
  }
}
