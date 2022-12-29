package com.tecdo.domain.request.n;

import com.tecdo.domain.base.Extension;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 用于原生广告支持的所有视频元素的视频对象
 */
@Getter
@Setter
public class Video extends Extension {

  /**
   * 支持的内容 MIME 类型
   */
  private List<String> mimes;

  /**
   * 最小的视频广告时间，以秒为单位
   */
  private Integer minduration;

  /**
   * 最大的视频广告时间，以秒为单位
   */
  private Integer maxduration;

  /**
   * 在出价响应中接受的一系列视频协议
   *
   * @see com.tecdo.enums.ProtocolsEnum
   */
  private List<Integer> protocols;

}
