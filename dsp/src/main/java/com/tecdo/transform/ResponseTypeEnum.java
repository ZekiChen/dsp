package com.tecdo.transform;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseTypeEnum {

  NORMAL(0, "normal"),

  FORCE(1, "force");

  private final int type;
  private final String desc;

}
