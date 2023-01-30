package com.tecdo.enums.biz;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 竞价策略
 **/
@Getter
@AllArgsConstructor
public enum BidStrategyEnum {

  CPM(1, "cpm"), CPC(2, "cpc");

  private final int type;
  private final String desc;

  public static BidStrategyEnum of(int type) {
    return Arrays.stream(BidStrategyEnum.values())
                 .filter(e -> e.type == type)
                 .findFirst()
                 .orElse(null);
  }
}
