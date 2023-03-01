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
        // 有些adx没有按照协议中ver的定义，比如传了1.2，但还是有native的wrapper
        NativeRequestWrapper nativeRequestWrapper =
          JsonHelper.parseObject(nativeRequestString, NativeRequestWrapper.class);
        if (nativeRequestWrapper != null && nativeRequestWrapper.getNativeRequest() != null) {
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
