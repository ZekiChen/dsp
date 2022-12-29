package com.tecdo.domain.response.n;

import com.tecdo.domain.base.Extension;

import lombok.Getter;
import lombok.Setter;

/**
 * @see com.tecdo.domain.request.n.Img 的响应
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
