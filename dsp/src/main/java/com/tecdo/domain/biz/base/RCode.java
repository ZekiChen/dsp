package com.tecdo.domain.biz.base;


import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
    SUCCESS(HttpResponseStatus.OK.code(), "操作成功"),

    UN_AUTHORIZED(HttpResponseStatus.UNAUTHORIZED.code(), "请求未授权"),
    NOT_FOUND(HttpResponseStatus.NOT_FOUND.code(), "没找到请求"),
    METHOD_NOT_SUPPORTED(HttpResponseStatus.METHOD_NOT_ALLOWED.code(), "不支持当前请求方法"),
    MEDIA_TYPE_NOT_SUPPORTED(HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE.code(), "不支持当前媒体类型"),
    REQ_REJECT(HttpResponseStatus.FORBIDDEN.code(), "请求被拒绝"),
    FAILURE(HttpResponseStatus.BAD_REQUEST.code(), "业务异常"),

    INTERNAL_SERVER_ERROR(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), "服务器内部异常，请联系管理员");

    // 状态码
    private final int code;
    // 状态响应描述信息
    private final String message;

}
