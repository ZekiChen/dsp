package com.tecdo.domain.response.n;

import com.tecdo.domain.base.Extension;

import lombok.Getter;
import lombok.Setter;

/**
 * @see com.tecdo.domain.request.n.Title 的响应
 */
@Getter
@Setter
public class Title extends Extension {

  /**
   * 需要展示的文本
   */
  private String text;
}
