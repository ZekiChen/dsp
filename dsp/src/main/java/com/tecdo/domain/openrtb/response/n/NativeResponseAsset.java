package com.tecdo.domain.openrtb.response.n;

import com.tecdo.domain.openrtb.base.Extension;
import com.tecdo.domain.openrtb.request.n.NativeRequestAsset;

import lombok.Getter;
import lombok.Setter;

/**
 * native广告的响应，title,img,video,data 这4个字段只能有一个存在，其他字段应该不存在或者为null
 *
 * @see NativeRequestAsset
 */
@Getter
@Setter
public class NativeResponseAsset extends Extension {

  /**
   * 唯一的id，必须和{@link NativeRequestAsset#id}对应
   */
  private Integer id;

  /**
   * 设置为1，如果Asset是被要求必须返回响应的，可以设置为 {@link NativeRequestAsset#required}的值
   */
  private Integer required;

  private Title title;

  private Img img;

  private Video video;

  private Data data;

  /**
   * 当前Asset被点击时的触发链接对象，当Asset不存在link时，则使用 {@link NativeResponse#link} 作为触发链接
   */
  private Link link;

}
