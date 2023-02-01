package com.tecdo.filter.util;

import cn.hutool.core.util.StrUtil;
import com.tecdo.exception.ConditionException;

import java.util.Arrays;
import java.util.Objects;

import static com.tecdo.filter.AbstractRecallFilter.Constant.*;

/**
 * 定投条件 工具
 *
 * Created by Zeki on 2023/1/5
 **/
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
                return Objects.equals(source, target);
            case GT:
                // todo 目前的数字比较还只有整形，但是后续可能会有小数的比较，比如osv就需要做处理
                return Integer.parseInt(source) > Integer.parseInt(target);
            case LT:
                return Integer.parseInt(source) < Integer.parseInt(target);
            case GTE:
                return Integer.parseInt(source) >= Integer.parseInt(target);
            case LTE:
                return Integer.parseInt(source) <= Integer.parseInt(target);
            case BETWEEN:
                String[] targetArr = target.split(",");
                if (targetArr.length != 2) {
                    throw new ConditionException("The value of the 'between' must be two numbers");
                }
                int sourceNum = Integer.parseInt(source);
                int num1 = Integer.parseInt(targetArr[0]);
                int num2 = Integer.parseInt(targetArr[1]);
                int small = Math.min(num1, num2);
                int big = small == num1 ? num2 : num1;
                return small <= sourceNum && sourceNum <= big;
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
