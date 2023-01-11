package com.tecdo.domain.biz.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * 公用返回值对象
 *
 * Created by Zeki on 2023/1/9
 **/
@Data
@NoArgsConstructor
public class R<T> implements Serializable {

    private static final long serialVersionUID = 6909760180903328409L;

    private int code;
    private String msg;
    private T data;
    private String version;

    private R(RCode rCode) {
        this(rCode, rCode.getMessage(), null);
    }

    private R(RCode rCode, String msg) {
        this(rCode, msg, null);
    }

    private R(RCode rCode, T data) {
        this(rCode, rCode.getMessage(), data);
    }

    private R(RCode rCode, String msg, T data) {
        this(rCode.getCode(), msg, data);
    }

    private R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 快速判断响应结果是否正确，不能用 isSuccess()，否则响应时会序列化多出 success 属性
    public boolean succeed() {
        return RCode.SUCCESS.getCode() == code;
    }

    // ======================== success ========================

    public static <T> R<T> success() {
        return new R<>(RCode.SUCCESS);
    }

    public static <T> R<T> success(String msg) {
        return new R<>(RCode.SUCCESS, msg);
    }

    public static <T> R<T> success(RCode rCode) {
        return new R<>(rCode);
    }

    public static <T> R<T> success(RCode rCode, String msg) {
        return new R<>(rCode, msg);
    }

    // ======================== failure ========================

    public static <T> R<T> failure() {
        return new R<>(RCode.FAILURE);
    }

    public static <T> R<T> failure(String msg) {
        return new R<>(RCode.FAILURE, msg);
    }

    public static <T> R<T> failure(int code, String msg) {
        return new R<>(code, msg, null);
    }

    public static <T> R<T> failure(RCode rCode) {
        return new R<>(rCode);
    }

    public static <T> R<T> failure(RCode rCode, String msg) {
        return new R<>(rCode, msg);
    }

    // ======================== data ========================

    public static <T> R<T> data(T data) {
        return new R<>(RCode.SUCCESS, data);
    }

    public static <T> R<T> data(String msg, T data) {
        return new R<>(RCode.SUCCESS, msg, data);
    }

    public static <T> R<T> data(RCode rCode, T data) {
        return new R<>(rCode, data);
    }

    // ======================== status ========================

    public static <T> R<T> status(boolean flag) {
        return flag ? success("操作成功") : failure("操作失败");
    }

}
