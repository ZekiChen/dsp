package com.tecdo.adm.api.delivery.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 竞价策略
 **/
@Getter
@AllArgsConstructor
public enum BidStrategyEnum {

  CPM(1, "CPM"),
  CPC(2, "TCPC"),
  CPA(3, "TPCA(Imp-Event1)"),
  DYNAMIC(4, "Base Price CPM"),
  CPA_EVENT1(5, "TPCA(Click-Event1)"),
  CPA_EVENT2(6, "TPCA(Click-Event2)"),
  CPA_EVENT3(7, "TPCA(Click-Event3)"),
  CPA_EVENT10(8, "TPCA(Click-Event10)"),
  OTHER(-1, "other");

  private final int type;
  private final String desc;

  public static BidStrategyEnum of(Integer type) {
    return type == null
      ? OTHER
      : Arrays.stream(BidStrategyEnum.values())
              .filter(e -> e.type == type)
              .findFirst()
              .orElse(OTHER);
  }
}
