package com.tecdo.enums.openrtb;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IP位置服务
 *
 * Created by Zeki on 2022/12/24
 **/
@Getter
@AllArgsConstructor
public enum IPLocationServiceEnum {

    IP2LOCATION("1", "ip2location"),
    NEUSTAR("2", "Neustar (Quova)"),
    MAXMIND("3", "MaxMind"),
    NETACUITY("4", "NetAcuity (Digital Element)");

    private final String value;
    private final String desc;
}
