package com.tecdo.filter.factory;

import cn.hutool.core.collection.CollUtil;
import com.tecdo.filter.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 广告召回 对象工厂
 *
 * Created by Zeki on 2023/1/3
 **/
@Getter
@Component
@RequiredArgsConstructor
public class RecallFiltersFactory {

    private final AffiliateFilter affiliateFilter;
    private final AppBundleFilter appBundleFilter;
    private final ConnectTypeFilter connectTypeFilter;
    private final DeviceLangFilter deviceLangFilter;
    private final CreativeFormatFilter creativeFormatFilter;
    private final DeviceCountryFilter deviceCountryFilter;
    private final DeviceMakeFilter deviceMakeFilter;
    private final DeviceOSFilter deviceOSFilter;
    private final DeviceOSVFilter deviceOSVFilter;
    private final TimePeriodFilter timePeriodFilter;
    private final BudgetFilter budgetFilter;
    private final ImpFrequencyFilter impFrequencyFilter;
    private final ClickFrequencyFilter clickFrequencyFilter;
    private final AfAudienceFilter afAudienceFilter;
    private final AffiliateBlockedAdFilter affiliateBlockedAdFilter;

    /**
     * 获取广告召回流程所需的全部过滤器集
     */
    public List<AbstractRecallFilter> createFilters() {
        return CollUtil.newArrayList(affiliateFilter,
                                     affiliateBlockedAdFilter,
                                     timePeriodFilter,
                                     deviceCountryFilter,
                                     deviceOSFilter,
                                     deviceOSVFilter,
                                     creativeFormatFilter,
                                     deviceMakeFilter,
                                     connectTypeFilter,
                                     deviceLangFilter,
                                     appBundleFilter,
                                     budgetFilter,
                                     afAudienceFilter,
                                     impFrequencyFilter,
                                     clickFrequencyFilter
                                     );
    }
}
