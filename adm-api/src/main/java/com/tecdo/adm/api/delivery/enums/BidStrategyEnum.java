package com.tecdo.adm.api.delivery.enums;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 竞价策略
 **/
@Getter
@AllArgsConstructor
public enum BidStrategyEnum {

  CPM(1, "cpm"),
  CPC(2, "cpc"),
  CPA(3, "cpa"),
  DYNAMIC(4, "dynamic"),
  CPA_EVENT1(5, "cpa_event1"),
  CPA_EVENT2(6, "cpa_event2"),
  CPA_EVENT3(7, "cpa_event3"),
  CPA_EVENT10(8, "cpa_event10"),
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
