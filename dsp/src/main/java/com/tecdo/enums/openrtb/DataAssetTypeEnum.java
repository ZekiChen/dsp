package com.tecdo.enums.openrtb;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum DataAssetTypeEnum {

  SPONSORED(1, "the brand name of the sponsor"),
  DESC(2, "Descriptive text associated with the product or service being advertised"),
  rating(3, "Rating of the product being offered to the user"),
  likes(4, "Number of social ratings or “likes” of the product being offered to the user"),
  downloads(5, "Number downloads/installs of this product"),
  price(6, "Price for product / app / in-app purchase."),
  saleprice(7,
            "Sale price that can be used together with price to indicate a discounted price compared to a regular price"),
  phone(8, "Phone number"),
  address(9, "Address"),
  desc2(10, "Additional descriptive text associated with the product or service being advertised"),
  displayurl(11,
             "Display URL for the text ad. To be used when sponsoring entity doesn’t own the content"),
  ctatext(12,
          "CTA description - descriptive text describing a ‘call to action’ button for the destination URL"),
  other(500, "oather");

  private final Integer value;
  private final String desc;

  public static DataAssetTypeEnum of(int value) {
    return Arrays.stream(DataAssetTypeEnum.values())
                 .filter(e -> e.value == value)
                 .findFirst()
                 .orElse(other);
  }
}
