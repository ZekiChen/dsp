package com.tecdo.transform;

import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Setter
public class YandexTransform extends AbstractTransform implements IProtoTransform {

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
