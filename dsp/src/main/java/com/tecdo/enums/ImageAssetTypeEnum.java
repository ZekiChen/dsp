package com.tecdo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ImageAssetTypeEnum {

  ICON(1, "Icon image"),
  /**
   * @deprecated To be deprecated in future version,use type 1 Icon.
   */
  LOGO(2, "Logo image for the brand/app"), MAIN(3, "Large image preview for the ad");

  /**
   * 值
   */
  private final Integer value;
  /**
   * 描述
   */
  private final String desc;
}
