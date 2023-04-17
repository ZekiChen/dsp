package com.tecdo.transform;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class InMobiTransform extends AbstractTransform implements IProtoTransform {

  private final String INMOBI_DEEPLINK = "inmobideeplink://navigate?primaryUrl=";

  @Override
  public String deepLinkFormat(String deepLink) {
    if (StringUtils.isNotBlank(deepLink)) {
      return INMOBI_DEEPLINK + encode(deepLink);
    }
    return deepLink;
  }
}
