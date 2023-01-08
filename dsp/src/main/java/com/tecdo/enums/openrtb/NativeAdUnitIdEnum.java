package com.tecdo.enums.openrtb;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
@Deprecated
public enum NativeAdUnitIdEnum {

  ONE(1, "Paid Search Units"),
  TWO(2, "Recommendation Widgets"),
  THREE(3, "Promoted Listings"),
  FOUR(4, "In-Ad (IAB Standard) with Native Element Units"),
  FIVE(5, "Custom /”Can’t Be Contained”");

  private final Integer value;
  private final String desc;
}
