package com.tecdo.enums.openrtb;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ContextTypeIdEnum {

  ONE(1,
      "Content-centric context such as newsfeed, article, image gallery, video gallery, or similar"),
  TWO(2, "Social-centric context such as social network feed, email, chat, or similar"),
  THREE(3,
        "Product context such as product listings, details, recommendations reviews, or similar");

  private final Integer value;
  private final String desc;
}
