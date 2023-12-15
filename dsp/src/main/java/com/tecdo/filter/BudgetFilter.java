package com.tecdo.filter;

import com.tecdo.common.constant.ConditionConstant;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.filter.util.ConditionHelper;
import com.tecdo.service.init.doris.BudgetManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 日预算控制 过滤
 * Created by Zeki on 2023/2/21
 */
@Component
public class BudgetFilter extends AbstractRecallFilter {

  @Autowired
  private BudgetManager budgetManager;

  @Override
  public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTOWrapper adDTOWrapper, Affiliate affiliate) {
    AdDTO adDTO = adDTOWrapper.getAdDTO();
    Double campaignBudget = adDTO.getCampaign().getDailyBudget();
    Double adGroupBudget = adDTO.getAdGroup().getDailyBudget();
    // 每个 campaign + adGroup 一天的消耗控制
    double campaignCost =
      budgetManager.getCampaignCost(adDTO.getCampaign().getId().toString()) / 1000;
    double adGroupCost =
      budgetManager.getAdGroupCost(adDTO.getAdGroup().getId().toString()) / 1000;

    return ConditionHelper.compare(Double.toString(campaignCost),
                                   ConditionConstant.LT,
                                   campaignBudget.toString()) &&
           ConditionHelper.compare(Double.toString(adGroupCost),
                                   ConditionConstant.LT,
                                   adGroupBudget.toString());
  }
}
