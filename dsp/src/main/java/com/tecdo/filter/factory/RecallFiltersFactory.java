package com.tecdo.filter.factory;

import com.tecdo.filter.AbstractRecallFilter;
import com.tecdo.filter.AffiliateFilter;
import com.tecdo.filter.AppBundleFilter;
import com.tecdo.filter.BudgetFilter;
import com.tecdo.filter.ClickFrequencyFilter;
import com.tecdo.filter.ConnectTypeFilter;
import com.tecdo.filter.CreativeFormatFilter;
import com.tecdo.filter.DeviceCountryFilter;
import com.tecdo.filter.DeviceLangFilter;
import com.tecdo.filter.DeviceMakeFilter;
import com.tecdo.filter.DeviceOSFilter;
import com.tecdo.filter.DeviceOSVFilter;
import com.tecdo.filter.ImpFrequencyFilter;
import com.tecdo.filter.TimePeriodFilter;

import org.springframework.stereotype.Component;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

/**
 * 广告召回 对象工厂
 *
 * Created by Zeki on 2023/1/3
 **/
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

    /**
     * 获取广告召回流程所需的全部过滤器集
     */
    public List<AbstractRecallFilter> createFilters() {
        return CollUtil.newArrayList(affiliateFilter,
                                     appBundleFilter,
                                     connectTypeFilter,
                                     deviceLangFilter,
                                     creativeFormatFilter,
                                     deviceCountryFilter,
                                     deviceMakeFilter,
                                     deviceOSFilter,
                                     deviceOSVFilter,
                                     timePeriodFilter,
                                     budgetFilter,
                                     impFrequencyFilter,
                                     clickFrequencyFilter);
    }
}
