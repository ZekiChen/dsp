package com.tecdo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
@Deprecated
public enum NativeLayoutIdEnum {

  ONE(1, "Content Wall"),
  TWO(2, "App Wall"),
  THREE(3, "News Feed"),
  FOUR(4, "Chat List"),
  FIVE(5, "Carousel"),
  SIX(6, "Content Stream"),
  SEVEN(7, "Grid adjoining the content");

  /**
   * 值
   */
  private final Integer value;
  /**
   * 描述
   */
  private final String desc;
}
