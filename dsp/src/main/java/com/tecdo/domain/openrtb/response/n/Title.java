package com.tecdo.domain.openrtb.response.n;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tecdo.domain.openrtb.base.Extension;

import lombok.Getter;
import lombok.Setter;

/**
 * native广告中title的响应
 *
 * @see com.tecdo.domain.openrtb.request.n.Title
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Title extends Extension {

  /**
   * 需要展示的文本
   */
  private String text;
}
