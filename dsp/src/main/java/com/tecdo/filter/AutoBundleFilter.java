package com.tecdo.filter;

import cn.hutool.core.util.StrUtil;
import com.sun.deploy.util.BlackList;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Zeki on 2023/10/23
 */
@Component
@RequiredArgsConstructor
public class AutoBundleFilter extends AbstractRecallFilter {

    private static final String AUTO_BUNDLE = ConditionEnum.AUTO_BUNDLE.getDesc();
    private static final String AUTO_BUNDLE_EXCEPT = ConditionEnum.AUTO_BUNDLE_EXCEPT.getDesc();

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        TargetCondition autoBundleCond = adDTO.getConditionMap().get(AUTO_BUNDLE);
        TargetCondition autoBundleExceptCond = adDTO.getConditionMap().get(AUTO_BUNDLE_EXCEPT);
        String bundleId = bidRequest.getApp().getBundle();

        // 黑白名单用集合表示
        Set<String> blackList = new HashSet<>();
        Set<String> whiteList = new HashSet<>();

        if (autoBundleCond != null && StrUtil.isNotBlank(autoBundleCond.getValue())) {
            Collections.addAll(blackList, autoBundleCond.getValue().split(","));
        }
        if (autoBundleExceptCond != null && StrUtil.isNotBlank(autoBundleExceptCond.getValue())) {
            Collections.addAll(whiteList, autoBundleExceptCond.getValue().split(","));
        }

        // 白名单包含bundle || 黑名单不包含bundle
        return whiteList.contains(bundleId) || !blackList.contains(bundleId);
    }
}
