package com.tecdo.domain.request.n;

import com.tecdo.domain.base.Extension;

import lombok.Getter;
import lombok.Setter;

/**
 * 每个对象中只能存在 {title,img,video,data} 对象之一，其他对象都应该是空的/不存在的
 * id在AssetObject数组中是唯一的，它是每个资产对象的ID，将响应映射到请求
 */
@Getter
@Setter
public class Asset extends Extension {

  /**
   * 唯一的资产 ID，由交易所分配
   */
  private Integer id;

  /**
   * 如果需要资产，则设置为 1（交易所不会接受没有它的出价）
   */
  private Integer required;

  private Title title;

  private Img img;

  private Video video;

  private Data data;


}
