package com.tecdo.transform;


import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.response.BidResponse;

public interface IProtoTransform {

  BidRequest requestTransform(String req);

  BidResponse responseTransform(AdDTOWrapper wrapper, BidRequest bidRequest, Affiliate affiliate);

}
