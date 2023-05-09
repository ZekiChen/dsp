package com.tecdo.transform;

import org.springframework.stereotype.Component;

@Component
public class O25N11Transform extends AbstractTransform implements IProtoTransform {

  @Override
  public String deepLinkFormat(String deepLink) {
    return deepLink;
  }

  @Override
  public boolean useBurl() {
    return false;
  }
}
