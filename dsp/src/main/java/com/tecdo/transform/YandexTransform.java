package com.tecdo.transform;

import org.springframework.stereotype.Component;

@Component
public class YandexTransform extends AbstractTransform implements IProtoTransform {

  @Override
  public String deepLinkFormat(String deepLink) {
    return deepLink;
  }

  @Override
  public boolean useBurl() {
    return true;
  }

  @Override
  public boolean buildAdmObject() {
    return false;
  }

  @Override
  public boolean useLossUrl() {
    return true;
  }

  @Override
  public boolean forceBannerEnable() {
    return false;
  }
}
