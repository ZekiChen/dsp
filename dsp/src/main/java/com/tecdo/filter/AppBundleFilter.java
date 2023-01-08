package com.tecdo.filter;

import cn.hutool.core.util.StrUtil;
import com.tecdo.domain.openrtb.request.App;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.TargetCondition;
import com.tecdo.filter.util.ConditionUtil;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Bundle过滤（仅App）
 *
 * Created by Zeki on 2023/1/3
 **/
@Component
public class AppBundleFilter extends AbstractRecallFilter {

    private static final String BUNDLE_ATTR = "app_bundle";

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, List<TargetCondition> conditions) {
        TargetCondition condition = conditions.stream().filter(e -> BUNDLE_ATTR.equals(e.getAttribute())).findFirst().orElse(null);
        if (condition == null) {
            return true;
        }
        App app = bidRequest.getApp();
        if (app == null || StrUtil.isBlank(app.getBundle())) {
            return false;
        }
        return ConditionUtil.compare(app.getBundle(), condition.getOperation(), condition.getValue());
    }
}
