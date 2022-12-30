package com.tecdo.transform;

import com.tecdo.domain.request.BidRequest;
import com.tecdo.domain.request.Imp;
import com.tecdo.domain.request.n.NativeRequest;
import com.tecdo.domain.response.BidResponse;
import com.tecdo.util.JsonHelper;

public class O25N11Transform implements IProtoTransform {

  private O25N11Transform() {
  }

  @Override
  public BidRequest requestTransform(String req) {
    BidRequest bidRequest = JsonHelper.parseObject(req, BidRequest.class);
    for (Imp imp : bidRequest.getImp()) {
      if (imp.getNative1() != null) {
        String nativeRequestString = imp.getNative1().getRequest();
        NativeRequest nativeRequest =
          JsonHelper.parseObject(nativeRequestString, NativeRequest.class);
        imp.getNative1().setNativeRequest(nativeRequest);
      }
    }
    return bidRequest;
  }

  @Override
  public BidResponse responseTransform(BidResponse bidResponse) {
    return bidResponse;
  }
}
