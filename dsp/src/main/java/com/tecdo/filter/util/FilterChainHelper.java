package com.tecdo.filter.util;

import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.filter.AbstractRecallFilter;
import com.tecdo.log.NotBidReasonLogger;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 *  过滤处理器链 工具
 *
 * Created by Zeki on 2023/1/3
 **/
@Slf4j
public class FilterChainHelper {

    /**
     * 过滤处理器组装成链
     */
    public static void assemble(List<AbstractRecallFilter> filters) {
        for (int i = 0; i < filters.size() - 1; i++) {
            filters.get(i).setNextFilter(filters.get(i + 1));
        }
    }

    /**
     * 每个 AD 都需要被所有 filter 判断一遍
     */
    public static boolean executeFilter(String bidId,
                                        AbstractRecallFilter curFilter,
                                        AdDTO adDTO,
                                        BidRequest bidRequest,
                                        Imp imp,
                                        Affiliate affiliate) {
        boolean filterFlag = curFilter.doFilter(bidRequest, imp, adDTO, affiliate);
        if (!filterFlag) {
            NotBidReasonLogger.log(bidId,
                                   adDTO.getAd().getId(),
                                   curFilter.getClass().getSimpleName());
            log.debug("ad recall fail, filter: {}", curFilter.getClass().getSimpleName());
        }
        while (filterFlag && curFilter.hasNext()) {
            curFilter = curFilter.getNextFilter();
            filterFlag = curFilter.doFilter(bidRequest, imp, adDTO, affiliate);
            if (!filterFlag) {
                NotBidReasonLogger.log(bidId,
                                       adDTO.getAd().getId(),
                                       curFilter.getClass().getSimpleName());
                log.debug("ad recall fail, filter: {}", curFilter.getClass().getSimpleName());
            }
        }
        return filterFlag;
    }
}
