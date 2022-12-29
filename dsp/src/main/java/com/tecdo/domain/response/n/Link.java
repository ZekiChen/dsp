package com.tecdo.domain.response.n;

import com.tecdo.domain.base.Extension;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Native广告点击时的跳转链接
 */
@Getter
@Setter
public class Link extends Extension {

  /**
   * 点击时唤醒的跳转链接
   */
  private String url;

  /**
   * 点击时触发的点击追踪上报链接
   */
  private List<String> clicktrackers;

  /**
   * 当不支持deeplink时的后备链接，也就是url失败时的后备跳转链接
   */
  private String fallback;

}
