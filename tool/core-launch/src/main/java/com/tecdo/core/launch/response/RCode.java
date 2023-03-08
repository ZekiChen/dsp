package com.tecdo.core.launch.response;

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
    SUCCESS(200, "操作成功"),

    UN_AUTHORIZED(401, "请求未授权"),
    NOT_FOUND(404, "没找到请求"),
    METHOD_NOT_SUPPORTED(405, "不支持当前请求方法"),
    MEDIA_TYPE_NOT_SUPPORTED(415, "不支持当前媒体类型"),
    REQ_REJECT(403, "请求被拒绝"),
    FAILURE(400, "业务异常"),

    INTERNAL_SERVER_ERROR(500, "服务器内部异常，请联系管理员");

    // 状态码
    private final int code;
    // 状态响应描述信息
    private final String message;

}
