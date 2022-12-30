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

    ADS_LOAD(1201, "ad dto list load"),
    ADS_LOAD_RESPONSE(1202, "ad dto list load response"),
    ADS_LOAD_ERROR(1203, "ad dto list load failure"),
    ADS_LOAD_TIMEOUT(1204, "ad dto list load timeout"),

    RTA_INFOS_LOAD(1301, "rta infos load"),
    RTA_INFOS_LOAD_RESPONSE(1302, "rta infos load response"),
    RTA_INFOS_LOAD_ERROR(1303, "rta infos load failure"),
    RTA_INFOS_LOAD_TIMEOUT(1304, "rta infos load timeout"),

    A_DATA_READY(1998, "A data finish init, enter ready, count increased"),
    NETTY_START(1999, "all data finish init, netty start"),

    CONTEXT_START(2001, "context start"),

    VALIDATE_BID_REQUEST(3001,"validate bid request"),
    RECEIVE_BID_REQUEST(3002,"receive bid request"),
    WAIT_TASK_RESPONSE_TIMEOUT(3003,"wait task response timeout"),

    RESPONSE_RESULT(6001,"response result"),
    ;

    private int code;
    private String desc;

    @Override
    public String toString() {
        return code + " - " + desc;
    }
}
