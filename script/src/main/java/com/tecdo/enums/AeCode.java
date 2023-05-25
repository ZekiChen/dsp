package com.tecdo.enums;

/**
 * Created by Zeki on 2023/4/3
 */
public enum AeCode {

    SUCCESS(0, "成功"),
    PARAM_ERROR(1001, "请求参数错误"),
    SIGN_ERROR(1002, "签名参数校验错误"),
    QPS_LIMIT(2001, "QPS超限"),
    DAILY_LIMIT(2002, "每日超限"),
    INTERNAL_ERROR(3001, "内部处理异常"),
    SYSTEM_CLOSE(4001, "系统暂时关闭");

    private final Integer code;
    private final String desc;

    AeCode(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
