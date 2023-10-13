package com.tecdo.filter;

import cn.hutool.core.util.StrUtil;
import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.adm.api.doris.dto.BundleCost;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.filter.util.ConditionHelper;
import com.tecdo.service.init.doris.BundleCostManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Elwin on 2023/10/12
 */
@Component
public class BundleFilter extends AbstractRecallFilter {
    @Autowired
    private BundleCostManager bundleCostManager;

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate) {
        String bundleId = bidRequest.getApp().getBundle();
        Integer groupId = adDTO.getAdGroup().getId();

        // 获取ad group曝光、点击、花费的定向条件
        TargetCondition defalut = new TargetCondition();
        defalut.setValue(null);
        String impCapDay = adDTO.getConditionMap()
                .getOrDefault(ConditionEnum.BUNDLE_IMP_CAP_DAY.getDesc(), defalut)
                .getValue();
        String clickCapDay = adDTO.getConditionMap()
                .getOrDefault(ConditionEnum.BUNDLE_CLICK_CAP_DAY.getDesc(), defalut)
                .getValue();
        String costCapDay = adDTO.getConditionMap()
                .getOrDefault(ConditionEnum.BUNDLE_COST_CAP_DAY.getDesc(), defalut)
                .getValue();

        // 当日bundle在该group下的imp, click, cost
        BundleCost bundleCost =
                bundleCostManager.getBundleCost(bundleId.concat(",").concat(groupId.toString()));

        // 构造过滤结果，未设限条件设置为true
        boolean impIsValid = StrUtil.isBlank(impCapDay) ||
                ConditionHelper.compare(bundleCost.getImpCount().toString(),
                                        Constant.LT,
                                        impCapDay);

        boolean clickIsValid = StrUtil.isBlank(clickCapDay) ||
                ConditionHelper.compare(bundleCost.getClickCount().toString(),
                                        Constant.LT,
                                        clickCapDay);

        boolean costIsValid = StrUtil.isBlank(costCapDay) ||
                ConditionHelper.compare(bundleCost.getBidPriceTotal().toString(),
                                        Constant.LT,
                                        costCapDay);

        return impIsValid && clickIsValid && costIsValid;
    }
}
