package com.tecdo.transform;

import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.domain.openrtb.request.n.NativeRequest;
import com.tecdo.domain.openrtb.request.n.NativeRequestWrapper;
import com.tecdo.domain.openrtb.response.BidResponse;
import com.tecdo.util.JsonHelper;

import org.springframework.stereotype.Component;

@Component
public class O25N11Transform implements IProtoTransform {

  private O25N11Transform() {
  }

  @Override
  public BidRequest requestTransform(String req) {
    BidRequest bidRequest = JsonHelper.parseObject(req, BidRequest.class);
    for (Imp imp : bidRequest.getImp()) {
      if (imp.getNative1() != null) {
        String nativeRequestString = imp.getNative1().getRequest();
        String ver = imp.getNative1().getVer();
        if ("1.0".equalsIgnoreCase(ver)) {
          NativeRequestWrapper nativeRequestWrapper =
            JsonHelper.parseObject(nativeRequestString, NativeRequestWrapper.class);
          imp.getNative1().setNativeRequestWrapper(nativeRequestWrapper);
          imp.getNative1().setNativeRequest(nativeRequestWrapper.getNativeRequest());
        } else {
          NativeRequest nativeRequest =
            JsonHelper.parseObject(nativeRequestString, NativeRequest.class);
          imp.getNative1().setNativeRequest(nativeRequest);
        }
      }
    }
    return bidRequest;
  }

  @Override
  public BidResponse responseTransform(BidResponse bidResponse) {
    return bidResponse;
  }
}
