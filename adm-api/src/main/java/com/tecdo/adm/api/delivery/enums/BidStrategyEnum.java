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

  CPM(1, "cpm"), CPC(2, "cpc");

  private final int type;
  private final String desc;

  public static BidStrategyEnum of(Integer type) {
    return type == null ? CPC :
            Arrays.stream(BidStrategyEnum.values())
                 .filter(e -> e.type == type)
                 .findFirst()
                 .orElse(null);
  }
}
