package com.tecdo.transform;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@Setter
public class InMobiTransform extends AbstractTransform implements IProtoTransform {

  private final String INMOBI_DEEPLINK = "inmobideeplink://navigate?primaryUrl=";

  private boolean forceBannerEnable = false;

  @Override
  public String deepLinkFormat(String deepLink) {
    if (StringUtils.isNotBlank(deepLink)) {
      return INMOBI_DEEPLINK + encode(deepLink);
    }
    return deepLink;
  }

  @Override
  public boolean useBurl() {
    return true;
  }

  @Override
  public boolean buildAdmObject() {
    return true;
  }

  @Override
  public boolean useLossUrl() {
    return true;
  }

  @Override
  public boolean forceBannerEnable() {
    return forceBannerEnable;
  }
}
