package com.tecdo.filter;

import com.tecdo.domain.biz.dto.AdDTO;
import com.tecdo.domain.openrtb.request.BidRequest;
import com.tecdo.domain.openrtb.request.Imp;
import com.tecdo.entity.Affiliate;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * 广告召回 抽象类
 *
 * Created by Zeki on 2023/1/3
 **/
@Setter
@Getter
public abstract class AbstractRecallFilter {

    public interface Constant {
        String EQ = "eq";  // 相同，value为单个值
        String GT = "gt";  // 大于，value为单个数字
        String LT = "lt";  // 小于，value为单个数字
        String GTE = "gte";  // 大于或等于，value为单个数字
        String LTE = "lte";  // 小于或等于，value为单个数字
        String BETWEEN = "between";  // 处于范围中间，value为包含两个数字的数组
        String INCLUDE = "include";  // 包含，value为数组
        String EXCLUDE = "exclude";  // 不包含，value为数组
        String CONTAINS = "contains";  // 不包含，value为数组
        String NOT_CONTAINS = "not_contains";  // 不包含，value为数组
    }

    private static final Set<String> OPERATION_SET = new HashSet<>();

    static {
        OPERATION_SET.add(Constant.EQ);
        OPERATION_SET.add(Constant.GT);
        OPERATION_SET.add(Constant.LT);
        OPERATION_SET.add(Constant.GTE);
        OPERATION_SET.add(Constant.LTE);
        OPERATION_SET.add(Constant.BETWEEN);
        OPERATION_SET.add(Constant.INCLUDE);
        OPERATION_SET.add(Constant.EXCLUDE);
    }

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
