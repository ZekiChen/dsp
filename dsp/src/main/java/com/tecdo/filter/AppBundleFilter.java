package com.tecdo.filter;

import cn.hutool.core.util.StrUtil;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.util.ConditionUtil;
import org.springframework.stereotype.Component;

/**
 * Bundle过滤（仅App）
 *
 * Created by Zeki on 2023/1/3
 **/
@Component
public class AppBundleFilter extends AbstractRecallFilter {

    private static final String BUNDLE_ATTR = "app_bundle";

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO) {
        TargetCondition condition = adDTO.getConditions().stream().filter(e -> BUNDLE_ATTR.equals(e.getAttribute())).findFirst().orElse(null);
        if (condition == null) {
            return true;
        }
        if (StrUtil.isBlank(bidRequest.getApp().getBundle())) {
            return false;
        }
        return ConditionUtil.compare(bidRequest.getApp().getBundle(), condition.getOperation(), condition.getValue());
    }
}
