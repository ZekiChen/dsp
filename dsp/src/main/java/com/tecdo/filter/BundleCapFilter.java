package com.tecdo.filter;

import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.adm.api.delivery.entity.TargetCondition;
import com.tecdo.adm.api.delivery.enums.ConditionEnum;
import com.tecdo.adm.api.doris.dto.BundleCost;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.biz.dto.AdDTOWrapper;
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
public class BundleCapFilter extends AbstractRecallFilter {
    @Autowired
    private BundleCostManager bundleCostManager;

    @Override
    public boolean doFilter(BidRequest bidRequest, Imp imp, AdDTOWrapper adDTOWrapper, Affiliate affiliate) {
        AdDTO adDTO = adDTOWrapper.getAdDTO();
        String bundleId = bidRequest.getApp().getBundle();
        Integer groupId = adDTO.getAdGroup().getId();

        // 获取ad group曝光、点击、花费的定向条件
        TargetCondition impCapDay = adDTO.getConditionMap()
                .get(ConditionEnum.BUNDLE_IMP_CAP_DAY.getDesc());
        TargetCondition clickCapDay = adDTO.getConditionMap()
                .get(ConditionEnum.BUNDLE_CLICK_CAP_DAY.getDesc());
        TargetCondition costCapDay = adDTO.getConditionMap()
                .get(ConditionEnum.BUNDLE_COST_CAP_DAY.getDesc());

        // 当日bundle在该group下的imp, click, cost
        BundleCost bundleCost =
                bundleCostManager.getBundleCost(bundleId.concat(",").concat(groupId.toString()));

        // 构造过滤结果，未设限条件设置为true
        boolean impIsValid = impCapDay == null ||
                ConditionHelper.compare(bundleCost.getImpCount().toString(),
                                        impCapDay.getOperation(),
                                        impCapDay.getValue());

        boolean clickIsValid = clickCapDay == null ||
                ConditionHelper.compare(bundleCost.getClickCount().toString(),
                                        clickCapDay.getOperation(),
                                        clickCapDay.getValue());

        boolean costIsValid = costCapDay == null ||
                ConditionHelper.compare(bundleCost.getCost().toString(),
                                        costCapDay.getOperation(),
                                        costCapDay.getValue());

        return impIsValid && clickIsValid && costIsValid;
    }
}
