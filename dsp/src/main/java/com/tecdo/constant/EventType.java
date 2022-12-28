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
    DB_DATA_INIT_COMPLETE(2, "db data init complete"),
    CONTEXT_START(3, "context start"),
    ;

    private int code;
    private String desc;

    @Override
    public String toString() {
        return code + " - " + desc;
    }
}
