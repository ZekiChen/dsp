package com.tecdo.ab.util;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.tecdo.exception.DspException;

import java.util.Arrays;
import java.util.Objects;

import static com.tecdo.common.constant.ConditionConstant.*;


public class ConditionHelper {

    /**
     * 基于操作符进行值比较
     *
     * @param source    源数据
     * @param operation 操作符
     * @param target    目标值
     * @return   true: 满足条件
     */
    public static boolean compare(String source, String operation, String target) {
        // 判空在上层已经校验过，调用该方法不会为空，否则就是预期意外的异常
        if (StrUtil.hasBlank(source, operation, target)) {
            throw new IllegalArgumentException("source/operation/value must not be blank");
        }

        switch (operation) {
            case EQ:
                return Objects.equals(source, target) ||
                       source.equalsIgnoreCase(target) ||
                       (NumberUtil.isNumber(source) && NumberUtil.isNumber(target) &&
                        Double.parseDouble(source) == Double.parseDouble(target));
            case GT:
                return Double.parseDouble(source) > Double.parseDouble(target);
            case LT:
                return Double.parseDouble(source) < Double.parseDouble(target);
            case GTE:
                return Double.parseDouble(source) >= Double.parseDouble(target);
            case LTE:
                return Double.parseDouble(source) <= Double.parseDouble(target);
            case BETWEEN:
                String[] targetArr = target.split(",");
                if (targetArr.length != 2) {
                    throw new DspException("The value of the 'between' must be two numbers");
                }
                double sourceNum = Double.parseDouble(source);
                double num1 = Double.parseDouble(targetArr[0]);
                double num2 = Double.parseDouble(targetArr[1]);
                if (num2 >= num1) {
                    return num1 <= sourceNum && sourceNum <= num2;
                } else {
                    // 比如控制小时投放，20点到8点可投放
                    return num1 <= sourceNum || sourceNum <= num2;
                }
            case INCLUDE:
                return Arrays.asList(target.split(",")).contains(source);
            case EXCLUDE:
                return !Arrays.asList(target.split(",")).contains(source);
            default:
                // 调用该方法不会是未被包含的操作符，否则就是预期意外的异常
                throw new IllegalArgumentException("Invalid operation: " + operation);
        }
    }
}
