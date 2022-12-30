package com.tecdo.domain.request.n;

import com.tecdo.domain.base.Extension;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 原生广告的请求，作为json字符串在{@link com.tecdo.domain.request.Native#request}中
 * 也有一部分交易所是选择将当前响应作为对象返回到{@link com.tecdo.domain.request.Native}的其他字段中
 * 标准的协议只支持JSON编码的字符串
 * <p>
 * 在1.1版本之前，当前请求是被包装多了一层map，作为native字段的值，{"native":NativeRequest},见{@link NativeRequestWrapper}
 * 再将这个map转为json字符串给{@link com.tecdo.domain.request.Native#request}
 */
@Getter
@Setter
public class NativeRequest extends Extension {

  /**
   * 正在使用的原生广告版本
   */
  private String ver;

  /**
   * @see com.tecdo.enums.NativeLayoutIdEnum
   * @deprecated 原生广告单元的布局 ID
   */
  private Integer layout;

  /**
   * @see com.tecdo.enums.NativeAdUnitIdEnum
   * @deprecated 原生广告单元的广告单元 ID
   */
  private Integer adunit;

  /**
   * 广告出现的上下文场景
   *
   * @see com.tecdo.enums.ContextTypeIdEnum
   */
  private Integer context;

  /**
   * 广告出现的上下文场景，更详细
   *
   * @see com.tecdo.enums.ContextSubTypeIdEnum
   */
  private Integer contextsubtype;

  /**
   * 提供的广告单元的设计/格式/布局
   *
   * @see com.tecdo.enums.PlacementTypeIdEnum
   */
  private Integer plcmttype;

  /**
   * 此布局中相同展示位置的数量
   */
  private Integer plcmtcnt;

  /**
   * 0 表示第一个广告，1 表示第二个广告，依此类推。请注意，这通常不会与 plcmtcnt 结合使用。
   * 要么您正在拍卖多个相同的展示位置（在这种情况下 plcmtcnt>1，seq=0）
   * 要么您正在为 Feed 中的不同项目进行单独拍卖（在这种情况下 plcmtcnt=1 , seq>=1)
   */
  private Integer seq;


  /**
   * 一组资产对象。 任何出价响应都必须符合出价请求中表达的元素数组
   */
  private List<Asset> assets;

}
