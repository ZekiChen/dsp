package com.tecdo.common.domain.biz;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.HttpStatus;

/**
 * 返回值各种状态枚举
 *
 * Created by Zeki on 2023/1/9
 **/
@Getter
@AllArgsConstructor
public enum RCode {

    /**
     * 常规状态码：2xx、3xx、4xx、5xx
     */
    SUCCESS(HttpStatus.SC_OK, "操作成功"),

    UN_AUTHORIZED(HttpStatus.SC_UNAUTHORIZED, "请求未授权"),
    NOT_FOUND(HttpStatus.SC_NOT_FOUND, "没找到请求"),
    METHOD_NOT_SUPPORTED(HttpStatus.SC_METHOD_NOT_ALLOWED, "不支持当前请求方法"),
    MEDIA_TYPE_NOT_SUPPORTED(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE, "不支持当前媒体类型"),
    REQ_REJECT(HttpStatus.SC_FORBIDDEN, "请求被拒绝"),
    FAILURE(HttpStatus.SC_BAD_REQUEST, "业务异常"),

    INTERNAL_SERVER_ERROR(HttpStatus.SC_INTERNAL_SERVER_ERROR, "服务器内部异常，请联系管理员");

    // 状态码
    private final int code;
    // 状态响应描述信息
    private final String message;

}
