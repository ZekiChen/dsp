package com.tecdo.service;

import cn.hutool.core.util.StrUtil;
import com.tecdo.adm.api.delivery.enums.AdTypeEnum;
import com.tecdo.adm.api.delivery.enums.BidStrategyEnum;
import com.tecdo.adm.api.doris.dto.ECPX;
import com.tecdo.domain.biz.BidCreative;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.service.init.doris.ECPXManager;
import com.tecdo.util.CreativeHelper;
import com.tecdo.util.FieldFormatHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Created by Zeki on 2023/7/31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BidPriceService {

    private final ECPXManager ecpxManager;

    public BigDecimal getECPX(BidStrategyEnum bidStrategy, BidRequest bidRequest, Imp imp) {
        String key = getDimensionsKey(bidRequest, imp);
        ECPX historyECPX = ecpxManager.getECPX(key);
        Double eCPX = null;
        switch (bidStrategy) {
            case CPC:
                eCPX = historyECPX.getECPC();
                break;
            case CPA:
            case CPA_EVENT1:
                eCPX = historyECPX.getECPAEvent1();
                break;
            case CPA_EVENT2:
                eCPX = historyECPX.getECPAEvent2();
                break;
            case CPA_EVENT3:
                eCPX = historyECPX.getECPAEvent3();
                break;
            case CPA_EVENT10:
                eCPX = historyECPX.getECPAEvent10();
                break;
        }
        return eCPX != null ? BigDecimal.valueOf(eCPX) : null;
    }

    // country_bundle_adFormat
    private String getDimensionsKey(BidRequest bidRequest, Imp imp) {
        String country = bidRequest.getDevice().getGeo().getCountry();
        String bundle = FieldFormatHelper.bundleIdFormat(bidRequest.getApp().getBundle());
        BidCreative bidCreative = CreativeHelper.getAdFormat(imp);
        String adFormat = Optional.ofNullable(bidCreative.getType())
                .map(AdTypeEnum::of)
                .map(AdTypeEnum::getDesc)
                .orElse(StrUtil.EMPTY);
        return country.concat("_").concat(bundle).concat("_").concat(adFormat);
    }
}
