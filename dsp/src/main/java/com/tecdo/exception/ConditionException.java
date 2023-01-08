package com.tecdo.exception;

/**
 * 当前 condition 的值不符合操作符要求
 * 举例：如 operation 为 "eq" ，而 value 为 "1,2"
 *
 * Created by Zeki on 2023/1/5
 **/
public class ConditionException extends RuntimeException {

    public ConditionException(String message) {
        super(message);
    }
}
