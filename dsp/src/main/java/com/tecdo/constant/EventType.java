package com.tecdo.constant;

import lombok.AllArgsConstructor;

/**
 * 事件类型
 *
 * Created by Zeki on 2022/12/27
 **/
@AllArgsConstructor
public enum EventType {

    SERVER_START(1001, "server start"),

    AFFILIATES_LOAD(1101, "affiliates load"),
    AFFILIATES_LOAD_RESPONSE(1102, "affiliates load response"),
    AFFILIATES_LOAD_ERROR(1103, "affiliates load failure"),
    AFFILIATES_LOAD_TIMEOUT(1104, "affiliates load timeout"),

    ADS_LOAD(1201, "ad list load"),
    ADS_LOAD_RESPONSE(1202, "ad list load response"),
    ADS_LOAD_ERROR(1203, "ad list load failure"),
    ADS_LOAD_TIMEOUT(1204, "ad list load timeout"),

    RTA_INFOS_LOAD(1301, "rta infos load"),
    RTA_INFOS_LOAD_RESPONSE(1302, "rta infos load response"),
    RTA_INFOS_LOAD_ERROR(1303, "rta infos load failure"),
    RTA_INFOS_LOAD_TIMEOUT(1304, "rta infos load timeout"),

//    DB_DATA_INIT_COMPLETE(1999, "db data init complete"),

    CONTEXT_START(2001, "context start"),
    ;

    private int code;
    private String desc;

    @Override
    public String toString() {
        return code + " - " + desc;
    }
}
