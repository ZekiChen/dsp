package com.tecdo.enums;

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

  /**
   * 值
   */
  private final Integer value;
  /**
   * 描述
   */
  private final String desc;
}
