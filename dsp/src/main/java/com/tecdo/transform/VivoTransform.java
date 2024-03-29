package com.tecdo.transform;

import cn.hutool.core.collection.CollUtil;
import com.tecdo.domain.openrtb.request.*;
import com.tecdo.util.StringConfigUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

  @Value("${pac.affiliate.vivo.default-bidfloor:5f}")
  private Float defaultBibFloor;

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
      if (geo == null) {
        geo = new Geo();
        device.setGeo(geo);
      }
      geo.setCountry(StringConfigUtil.getCountryCode3(device.getRegion()));
    }

    List<Imp> imps = bidRequest.getImp();
    if (CollUtil.isNotEmpty(imps)) {
      imps.forEach(imp -> {
        float bidfloor = BigDecimal.valueOf(imp.getBidFloor())
                .divide(BigDecimal.valueOf(100), 3, RoundingMode.HALF_UP)
                .floatValue();
        // vivo有部分流量底价为 0 美分，直接初始化 5 美分，便于贴底价买量处理
        imp.setBidfloor(bidfloor == 0f ? defaultBibFloor : bidfloor);

        Integer impType = imp.getImpType();
        if (impType != null && impType == 1) {
          imp.setNative1(NATIVE_EMPTY);
        }
      });
    }

    return bidRequest;
  }
}
