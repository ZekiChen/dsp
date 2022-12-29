package com.tecdo.domain.response.n;

import com.tecdo.domain.base.Extension;

import lombok.Getter;
import lombok.Setter;

/**
 * @see com.tecdo.domain.request.n.Video 的响应
 * video响应是VAST协议的xml
 */
@Getter
@Setter
public class Video extends Extension {

  /**
   * vast xml.
   */
  private String vasttag;
}
