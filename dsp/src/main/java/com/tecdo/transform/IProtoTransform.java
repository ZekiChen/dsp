package com.tecdo.transform;


import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.response.BidResponse;

public interface IProtoTransform {

  BidRequest requestTransform(String req);

  BidResponse responseTransform(BidResponse bidResponse);

}
