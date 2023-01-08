package com.tecdo.domain.openrtb.request.n;

import com.tecdo.domain.openrtb.base.Extension;
import com.tecdo.enums.openrtb.ImageAssetTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 用于原生广告的所有图像元素（例如图标、主图像等）的图像对象
 */
@Getter
@Setter
public class Img extends Extension {

  /**
   * 发布者支持的图像元素的类型
   *
   * @see ImageAssetTypeEnum
   */
  private Integer type;

  /**
   * 图像的宽度，以像素为单位
   */
  private Integer w;

  /**
   * 图像的最小要求宽度，单位是像素。这个选项应该用于客户端对图像的任何重新缩放。w或wmin都应该被传送。如果只包括w，应该被认为是一个精确的要求
   */
  private Integer wmin;

  /**
   * 图像的高度，以像素为单位
   */
  private Integer h;

  /**
   * 图像的最小要求高度，单位是像素。这个选项应该用于客户端对图像的任何重新缩放。h或hmin都应该被传送。如果只包括h，应该被认为是一个精确的要求
   */
  private Integer hmin;

  /**
   * 支持的内容MIME类型的白名单。流行的MIME类型包括，但不限于 "image/jpg" "image/gif"。每个Adx都应该在集成文档中有自己的支持类型列表。
   * 如果为空，则假定允许所有类型
   */
  private List<String> mimes;

}
