package com.tecdo.domain.openrtb.response.n;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tecdo.domain.openrtb.base.Extension;
import com.tecdo.domain.openrtb.response.Bid;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 原生广告的响应，应该作为json字符串在{@link Bid#adm}中返回
 * 也有一部分交易所是选择将当前响应作为对象返回到{@link Bid}的其他字段中
 * 标准协议只支持JSON编码的字符串
 * <p>
 * 在1.1版本之前，当前响应是被包装多了一层map，作为native字段的值，{"native":NativeResponse}见{@link NativeResponseWrapper}
 * 再将这个map转为json字符串返回给{@link Bid#adm}
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NativeResponse extends Extension {

  /**
   * 当前的原生广告的版本
   */
  private String ver;

  /**
   * 原生广告的资产列表
   */
  private List<NativeResponseAsset> assets;

  /**
   * 广告的默认点击链接，Asset也可以有自己的link，如果Asset没有自己的link对象，则采用这个link
   */
  private Link link;

  /**
   * 曝光通知链接，链接应该返回1*1的图像或者204响应
   */
  private List<String> imptrackers;

  /**
   * 可选的js追踪代码，这是一个有效的HTML，js已经被包含在<scrip>标签中
   * 它必须支持在曝光的时候自动执行
   */
  private String jstracker;


}
