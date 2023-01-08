package com.tecdo.domain.openrtb.request.n;

import com.tecdo.domain.openrtb.base.Extension;
import com.tecdo.enums.openrtb.ProtocolsEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
   * @see ProtocolsEnum
   */
  private List<Integer> protocols;

}
