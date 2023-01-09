package com.tecdo.filter.factory;

import cn.hutool.core.collection.CollUtil;
import com.tecdo.filter.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

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
    private final ContentLangFilter contentLangFilter;
    private final CreativeFormatFilter creativeFormatFilter;
    private final DeviceCountryFilter deviceCountryFilter;
    private final DeviceMakeFilter deviceMakeFilter;
    private final DeviceOSFilter deviceOSFilter;
    private final DeviceOSVFilter deviceOSVFilter;
    private final TimePeriodFilter timePeriodFilter;

    /**
     * 获取广告召回流程所需的全部过滤器集
     */
    public List<AbstractRecallFilter> createFilters() {
        return CollUtil.newArrayList(
                affiliateFilter, appBundleFilter, connectTypeFilter, contentLangFilter, creativeFormatFilter,
                deviceCountryFilter, deviceMakeFilter, deviceOSFilter, deviceOSVFilter, timePeriodFilter
        );
    }
}
