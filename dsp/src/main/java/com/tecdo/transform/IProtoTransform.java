package com.tecdo.transform;

import com.tecdo.domain.request.BidRequest;
import com.tecdo.domain.response.BidResponse;

public interface IProtoTransform {

  BidRequest requestTransform(String req);

  BidResponse responseTransform(BidResponse bidResponse);

}
