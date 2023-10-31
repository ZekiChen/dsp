package com.tecdo.common.constant;

/**
 * Created by Elwin on 2023/10/27
 */
public interface ConditionConstant {
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
