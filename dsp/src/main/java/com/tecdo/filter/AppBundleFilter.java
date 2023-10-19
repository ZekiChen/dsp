package com.tecdo.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.filter.util.ConditionHelper;
import com.tecdo.service.init.doris.GooglePlayAppManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bundle过滤（仅App）
 *
 * Created by Zeki on 2023/1/3
 **/
@Component
@RequiredArgsConstructor
public class AppBundleFilter extends AbstractRecallFilter {

    private final GooglePlayAppManager googlePlayAppManager;

    private static final String BUNDLE = ConditionEnum.BUNDLE.getDesc();
    private static final String CATEGORY = ConditionEnum.CATEGORY.getDesc();
    private static final String TAG = ConditionEnum.TAG.getDesc();

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        TargetCondition bundleCond = adDTO.getConditionMap().get(BUNDLE);
        TargetCondition categoryCond = adDTO.getConditionMap().get(CATEGORY);
        TargetCondition tagCond = adDTO.getConditionMap().get(TAG);
        String sourceBundle = bidRequest.getApp().getBundle();
        if (StrUtil.isBlank(sourceBundle)) {
            return false;
        }
        List<String> tarBundles = new ArrayList<>();
        if (categoryCond != null) {
            for (String category : categoryCond.getValue().split(StrUtil.COMMA)) {
                tarBundles.addAll(googlePlayAppManager.listByCategory(category));
            }
        }
        if (tagCond != null) {
            for (String tag : tagCond.getValue().split(StrUtil.COMMA)) {
                tarBundles.addAll(googlePlayAppManager.listByTag(tag));
            }
        }
        if (CollUtil.isEmpty(tarBundles)) {
            return bundleCond == null ||
                    ConditionHelper.compare(sourceBundle, bundleCond.getOperation(), bundleCond.getValue());
        } else {
            if (bundleCond != null) {
                List<String> bundles = Arrays.asList(bundleCond.getValue().split(StrUtil.COMMA));
                if (Constant.INCLUDE.equals(bundleCond.getOperation())) {
                    tarBundles.addAll(bundles);
                } else {
                    tarBundles = tarBundles.stream().filter(tar -> !bundles.contains(tar)).collect(Collectors.toList());
                }
            }
            return tarBundles.stream().anyMatch(i -> i.equalsIgnoreCase(sourceBundle));
        }
    }
}
