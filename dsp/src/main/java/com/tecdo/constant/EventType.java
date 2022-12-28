package com.tecdo.constant;

import lombok.AllArgsConstructor;

/**
 * 事件类型
 *
 * Created by Zeki on 2022/12/27
 **/
@AllArgsConstructor
public enum EventType {

    SERVER_START(1, "server start"),

    AFFILIATES_LOAD(2, "affiliates load"),
    AFFILIATES_LOAD_RESPONSE(3, "affiliates load response"),
    AFFILIATES_LOAD_SUCCESS(4, "affiliates load success"),
    AFFILIATES_LOAD_TIMEOUT(5, "affiliates load timeout"),

    DB_DATA_INIT_COMPLETE(4, "db data init complete"),

    CONTEXT_START(5, "context start"),
    ;

    private int code;
    private String desc;

    @Override
    public String toString() {
        return code + " - " + desc;
    }
}
