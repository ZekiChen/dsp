package com.tecdo.filter;

import com.tecdo.adm.api.delivery.entity.Affiliate;
import com.tecdo.common.constant.ConditionConstant;
import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import lombok.Getter;
import lombok.Setter;

/**
 * 广告召回 抽象类
 *
 * Created by Zeki on 2023/1/3
 **/
@Setter
@Getter
public abstract class AbstractRecallFilter implements ConditionConstant {

    /**
     * 下一个过滤器
     */
    protected AbstractRecallFilter nextFilter;

    public boolean hasNext() {
        return nextFilter != null;
    }

    /**
     * 判断当前的AD是否需要召回
     *
     * @param bidRequest 竞价请求对象
     * @param imp        展示对象
     * @param adDTO      AD完整数据
     * @return           true: 召回
     */
    public abstract boolean doFilter(BidRequest bidRequest, Imp imp, AdDTO adDTO, Affiliate affiliate);
}
