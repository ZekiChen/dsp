package com.tecdo.filter;

import com.tecdo.domain.request.BidRequest;
import com.tecdo.domain.request.Imp;
import com.tecdo.entity.TargetCondition;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Zeki on 2023/1/3
 **/
@Component
public class AffiliateFilter extends AbstractRecallFilter {

    private static final String AFF_ATTR = "affiliate";

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, List<TargetCondition> conditions) {
        // TODO
        return false;
    }
}
