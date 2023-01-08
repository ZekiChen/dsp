package com.tecdo.domain.openrtb.request.n;

import com.tecdo.domain.openrtb.base.Extension;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Title extends Extension {

  /**
   * 标题元素中文本的最大长度。
   */
  private Integer len;

}
