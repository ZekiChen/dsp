package com.tecdo.domain.response.n;

import com.tecdo.domain.base.Extension;

import lombok.Getter;
import lombok.Setter;

/**
 * @see com.tecdo.domain.request.n.Data 的响应
 */
@Getter
@Setter
public class Data extends Extension {

  /**
   * 要显示的数据类型的可选格式化字符串名称
   */
  private String label;

  /**
   * 需要显示的格式化的数据
   */
  private String value;

}
