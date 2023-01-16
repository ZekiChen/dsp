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

    ONE_DATA_READY(1998, "a data finish init, enter ready, count increased"),
    NETTY_START(1999, "all data finish init, netty start"),

    CONTEXT_START(2001, "context start"),

    TASK_START(3001, "task start"),
    ADS_RECALL_FINISH(3002, "ads recall finish"),
    ADS_RECALL_ERROR(3003, "ads recall error"),
    ADS_RECALL_TIMEOUT(3004, "ads recall timeout"),

    CTR_PREDICT_FINISH(3101, "ctr predict finish"),
    CTR_PREDICT_ERROR(3102, "ctr predict error"),
    CTR_PREDICT_TIMEOUT(3103, "ctr predict timeout"),

    CALC_CPC_FINISH(3201, "calculate cpc finish"),
    CALC_CPC_ERROR(3202, "calculate cpc error"),
    CALC_CPC_TIMEOUT(3203, "calculate cpc timeout"),
    ;

    private final int code;
    private final String desc;

    @Override
    public String toString() {
        return code + " - " + desc;
    }
}
