package com.tecdo.domain.request.n;

import com.tecdo.domain.base.Extension;

import lombok.Getter;
import lombok.Setter;

/**
 * 用于原生单元的所有非核心元素，例如品牌名称、评级、评论计数、星级、下载计数、描述等
 */
@Getter
@Setter
public class Data extends Extension {

  /**
   * 当前文本代表什么
   *
   * @see com.tecdo.enums.DataAssetTypeEnum
   */
  private Integer type;

  /**
   * 支持返回的最大文本长度
   */
  private Integer len;

}
