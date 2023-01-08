package com.tecdo.enums.openrtb;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum PlacementTypeIdEnum {

  ONE(1,
      "In the feed of content - for example as an item inside the organic feed/grid/listing/carousel"),
  TWO(2, "In the atomic unit of the content - IE in the article page or single image page"),
  THREE(3,
        "Outside the core content - for example in the ads section on the right rail, as a banner-style placement near the content, etc"),
  FOUR(4, "Recommendation widget, most commonly presented below the article content");

  private final Integer value;
  private final String desc;
}
