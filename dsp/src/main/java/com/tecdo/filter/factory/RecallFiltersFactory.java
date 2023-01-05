package com.tecdo.filter.factory;

import cn.hutool.core.collection.CollUtil;
import com.tecdo.filter.AbstractRecallFilter;
import com.tecdo.filter.AffiliateFilter;
import com.tecdo.filter.CountryFilter;
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

    private final CountryFilter countryFilter;
    private final AffiliateFilter affiliateFilter;

    /**
     * 获取广告召回流程所需的全部过滤器集
     */
    public List<AbstractRecallFilter> createFilters() {
        return CollUtil.newArrayList(countryFilter, affiliateFilter);
    }
}
