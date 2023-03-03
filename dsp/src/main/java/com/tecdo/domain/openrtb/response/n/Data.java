package com.tecdo.domain.openrtb.response.n;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tecdo.domain.openrtb.base.Extension;

import lombok.Getter;
import lombok.Setter;

/**
 * native广告中data的响应
 *
 * @see com.tecdo.domain.openrtb.request.n.Data
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
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
