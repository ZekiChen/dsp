package com.tecdo.transform;

import com.tecdo.util.ParamHelper;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@Setter
public class InMobiTransform extends AbstractTransform implements IProtoTransform {

  private final String INMOBI_DEEPLINK = "inmobideeplink://navigate?primaryUrl=";

  @Override
  public boolean forceBannerEnable() {
    return false;
  }

  @Override
  public String deepLinkFormat(String deepLink) {
    if (StringUtils.isNotBlank(deepLink)) {
      return INMOBI_DEEPLINK + ParamHelper.encode(deepLink);
    }
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
    return true;
  }
}
