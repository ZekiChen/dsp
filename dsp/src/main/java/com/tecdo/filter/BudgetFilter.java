package com.tecdo.filter;

import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.Affiliate;
import com.tecdo.filter.util.ConditionHelper;
import com.tecdo.service.init.BudgetManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 日预算控制 过滤
 *
 * Created by Zeki on 2023/2/21
 */
@Component
@RequiredArgsConstructor
public class BudgetFilter extends AbstractRecallFilter {

  private final BudgetManager budgetManager;

  @Override
  public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
    Double campaignBudget = adDTO.getCampaign().getDailyBudget();
    Double adGroupBudget = adDTO.getAdGroup().getDailyBudget();
    // 每个 campaign + adGroup 一天的消耗控制
    Double cost = budgetManager.getBudget(adDTO.getCampaign().getId().toString(), adDTO.getAdGroup().getId().toString());
    return cost != null
            && ConditionHelper.compare(cost.toString(), Constant.LT, campaignBudget.toString())
            && ConditionHelper.compare(cost.toString(), Constant.LT, adGroupBudget.toString());
  }
}
