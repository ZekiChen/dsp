package com.tecdo.filter.factory;

import cn.hutool.core.collection.CollUtil;
import com.tecdo.filter.*;
import com.tecdo.filter.util.FilterChainHelper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 广告召回 对象工厂
 * <p>
 * Created by Zeki on 2023/1/3
 **/
@Getter
@Component
public class RecallFiltersFactory {

    private final List<AbstractRecallFilter> filterChain;

    private final AffiliateFilter affiliateFilter;
    private final AppBundleFilter appBundleFilter;
    private final ConnectTypeFilter connectTypeFilter;
    private final DeviceLangFilter deviceLangFilter;
    private final CreativeFormatFilter creativeFormatFilter;
    private final AdPositionFilter adPositionFilter;
    private final InterstitialFilter interstitialFilter;
    private final VideoPlacementFilter videoPlacementFilter;
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
    private final BundleCapFilter bundleCapFilter;

    @Autowired
    public RecallFiltersFactory(AffiliateFilter affiliateFilter,
                                AppBundleFilter appBundleFilter,
                                ConnectTypeFilter connectTypeFilter,
                                DeviceLangFilter deviceLangFilter,
                                CreativeFormatFilter creativeFormatFilter,
                                AdPositionFilter adPositionFilter,
                                InterstitialFilter interstitialFilter,
                                VideoPlacementFilter videoPlacementFilter,
                                DeviceCountryFilter deviceCountryFilter,
                                DeviceMakeFilter deviceMakeFilter,
                                DeviceOSFilter deviceOSFilter,
                                DeviceOSVFilter deviceOSVFilter,
                                TimePeriodFilter timePeriodFilter,
                                BudgetFilter budgetFilter,
                                ImpFrequencyFilter impFrequencyFilter,
                                ClickFrequencyFilter clickFrequencyFilter,
                                AfAudienceFilter afAudienceFilter,
                                AffiliateBlockedAdFilter affiliateBlockedAdFilter,
                                BundleCapFilter bundleCapFilter) {

        this.affiliateFilter = affiliateFilter;
        this.appBundleFilter = appBundleFilter;
        this.connectTypeFilter = connectTypeFilter;
        this.deviceLangFilter = deviceLangFilter;
        this.creativeFormatFilter = creativeFormatFilter;
        this.adPositionFilter = adPositionFilter;
        this.interstitialFilter = interstitialFilter;
        this.videoPlacementFilter = videoPlacementFilter;
        this.deviceCountryFilter = deviceCountryFilter;
        this.deviceMakeFilter = deviceMakeFilter;
        this.deviceOSFilter = deviceOSFilter;
        this.deviceOSVFilter = deviceOSVFilter;
        this.timePeriodFilter = timePeriodFilter;
        this.budgetFilter = budgetFilter;
        this.impFrequencyFilter = impFrequencyFilter;
        this.clickFrequencyFilter = clickFrequencyFilter;
        this.afAudienceFilter = afAudienceFilter;
        this.affiliateBlockedAdFilter = affiliateBlockedAdFilter;
        this.bundleCapFilter = bundleCapFilter;

        List<AbstractRecallFilter> filters = CollUtil.newArrayList(
                affiliateFilter,
                affiliateBlockedAdFilter,
                timePeriodFilter,
                deviceCountryFilter,
                deviceOSFilter,
                deviceOSVFilter,
                creativeFormatFilter,
                adPositionFilter,
                interstitialFilter,
                videoPlacementFilter,
                deviceMakeFilter,
                connectTypeFilter,
                deviceLangFilter,
                appBundleFilter,
                budgetFilter,
                afAudienceFilter,
                impFrequencyFilter,
                clickFrequencyFilter,
                bundleCapFilter
        );
        FilterChainHelper.assemble(filters);
        this.filterChain = filters;
    }

    /**
     * 获取广告召回流程所需的全部过滤器集
     */
    public List<AbstractRecallFilter> createFilters() {
        return this.filterChain;
    }

}
