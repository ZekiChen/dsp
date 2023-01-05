package com.tecdo.filter.util;

import com.tecdo.filter.AbstractRecallFilter;

/**
 * 基于指定操作符进行值匹配 工具
 *
 * Created by Zeki on 2023/1/5
 **/
public class ConditionCompareUtil {

    public static boolean compare(String source, String operation, String target) {
        switch (operation) {
            case AbstractRecallFilter.Constant.EQ:
//                return source == target;
            case AbstractRecallFilter.Constant.GT:
//                return source > target;
            case AbstractRecallFilter.Constant.LT:
//                return source < target;
            case AbstractRecallFilter.Constant.GTE:
//                return source >= target;
            case AbstractRecallFilter.Constant.LTE:
//                return source <= target;
                // TODO 其他的操作也可以添加
            default:
                throw new IllegalArgumentException("Invalid operation: " + operation);
        }
    }
}
