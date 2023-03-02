package com.tecdo.domain.openrtb.response.n;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tecdo.domain.openrtb.base.Extension;

import lombok.Getter;
import lombok.Setter;

/**
 * native广告中video素材的响应，video响应是VAST协议的xml
 *
 * @see com.tecdo.domain.openrtb.request.n.Video
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Video extends Extension {

  /**
   * vast xml.
   */
  private String vasttag;
}
