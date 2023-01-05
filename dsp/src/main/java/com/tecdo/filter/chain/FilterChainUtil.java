package com.tecdo.filter.chain;

import com.tecdo.filter.AbstractRecallFilter;

import java.util.List;

/**
 *  过滤处理器链 工具
 *
 * Created by Zeki on 2023/1/3
 **/
public class FilterChainUtil {

    /**
     * 过滤处理器组装成链
     */
    public static void assemble(List<AbstractRecallFilter> filters) {
        for (int i = 0; i < filters.size() - 1; i++) {
            filters.get(i).setNextFilter(filters.get(i + 1));
        }
    }
}
