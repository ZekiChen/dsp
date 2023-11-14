package com.tecdo.adm.api.delivery.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 出价模式
 *
 * Created by Zeki on 2023/11/6
 */
@Getter
@AllArgsConstructor
public enum BidModeEnum {

  BASE_BID(0, "Base Bid"),
  TWO_STAGE_BID(1, "Two-Stage Bidding"),
  OTHER(-1, "other");

  private final int type;
  private final String desc;

  public static BidModeEnum of(int type) {
    return Arrays.stream(BidModeEnum.values()).filter(e -> e.type == type).findAny().orElse(OTHER);
  }
}
