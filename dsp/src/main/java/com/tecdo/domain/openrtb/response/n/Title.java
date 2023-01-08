package com.tecdo.domain.openrtb.response.n;

import com.tecdo.domain.openrtb.base.Extension;

import lombok.Getter;
import lombok.Setter;

/**
 * @see com.tecdo.domain.openrtb.request.n.Title 的响应
 */
@Getter
@Setter
public class Title extends Extension {

  /**
   * 需要展示的文本
   */
  private String text;
}
