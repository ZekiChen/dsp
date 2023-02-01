package com.tecdo.filter;

import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.Affiliate;
import com.tecdo.filter.util.ConditionHelper;

import org.springframework.stereotype.Component;

@Component
public class BudgetFilter extends AbstractRecallFilter {

  @Override
  public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
    Double campaignBudget = adDTO.getCampaign().getDailyBudget();
    Double adGroupBudget = adDTO.getAdGroup().getDailyBudget();
    // todo get campaign and adGroup realtime cost
    return ConditionHelper.compare("0", Constant.LT, campaignBudget.toString()) &&
           ConditionHelper.compare("0", Constant.LT, adGroupBudget.toString());
  }
}
