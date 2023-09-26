package com.tecdo.transform;

import cn.hutool.core.collection.CollUtil;
import com.tecdo.domain.openrtb.request.*;
import com.tecdo.util.StringConfigUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VivoTransform extends AbstractTransform implements IProtoTransform {

  private static final Native NATIVE_EMPTY = new Native();

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

  @Override
  public BidRequest requestTransform(String req) {
    BidRequest bidRequest = super.requestTransform(req);
    if (bidRequest == null) {
      return null;
    }

    Device device = bidRequest.getDevice();
    if (device != null) {
      device.setIfa(device.getDid());
      Geo geo = device.getGeo();
      if (geo != null) {
        geo.setCountry(StringConfigUtil.getCountryCode3(device.getRegion()));
      }
    }

    List<Imp> imps = bidRequest.getImp();
    if (CollUtil.isNotEmpty(imps)) {
      imps.forEach(imp -> {
        imp.setBidfloor(imp.getBidFloor() / 100);

        Integer impType = imp.getImpType();
        if (impType != null && impType == 1) {
          imp.setNative1(NATIVE_EMPTY);
        }
      });
    }

    return bidRequest;
  }
}
