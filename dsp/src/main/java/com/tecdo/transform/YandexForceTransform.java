package com.tecdo.transform;

import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Setter
public class YandexForceTransform extends AbstractTransform implements IProtoTransform {

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
    return true;
  }
}
