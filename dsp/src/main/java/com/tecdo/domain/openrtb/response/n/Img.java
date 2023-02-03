package com.tecdo.domain.openrtb.response.n;

import com.tecdo.domain.openrtb.base.Extension;

import lombok.Getter;
import lombok.Setter;

/**
 * native广告中img的响应
 *
 * @see com.tecdo.domain.openrtb.request.n.Img
 */
@Getter
@Setter
public class Img extends Extension {

  /**
   * 图像的url地址
   */
  private String url;

  /**
   * 图像的宽度，以像素为单位
   */
  private Integer w;

  /**
   * 图像的高度，以像素为单位
   */
  private Integer h;


}
