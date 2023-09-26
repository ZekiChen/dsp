package com.tecdo.transform;


import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.response.BidResponse;

import java.util.Map;

public interface IProtoTransform {

  BidRequest requestTransform(String req);

  BidResponse responseTransform(Map<String, AdDTOWrapper> impBidAdMap, BidRequest bidRequest, Affiliate affiliate);

  ResponseTypeEnum getResponseType(String forceLink, AdDTOWrapper wrapper);

}
