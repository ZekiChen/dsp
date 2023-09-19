package com.tecdo.transform;

import org.springframework.stereotype.Component;

@Component
public class VivoTransform extends AbstractTransform implements IProtoTransform {

  @Override
  public boolean forceBannerEnable() {
    return false;
  }

  @Override
  public String deepLinkFormat(String deepLink) {
    return deepLink;
  }

  @Override
  public boolean useBurl() {
    return false;
  }

  @Override
  public boolean useLossUrl() {
    return true;
  }

  @Override
  public boolean buildAdmByImmobi() {
    return false;
  }
}
