package com.tecdo.transform;

import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Setter
public class YandexForceTransform extends AbstractTransform implements IProtoTransform {

  @Override
  public boolean forceBannerEnable() {
    return true;
  }

  @Override
  public String deepLinkFormat(String deepLink) {
    return deepLink;
  }

  @Override
  public boolean useBurl() {
    return true;
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
