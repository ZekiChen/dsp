package com.tecdo.filter;

import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Device;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.Affiliate;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.util.ConditionUtil;

import java.util.Optional;

public class ClickFrequencyFilter extends AbstractRecallFilter {

  private static final String CLICK_FREQUENCY_ATTR = "click_frequency";

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
    String deviceId = Optional.ofNullable(bidRequest.getDevice()).map(Device::getIfa).orElse(null);
    // todo get realtime frequency
    Integer frequency = 0;

    return ConditionUtil.compare(String.valueOf(frequency),
                                 condition.getOperation(),
                                 condition.getValue());
  }
}
