package com.tecdo.enums.openrtb;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 下表列出了竞价者向ADX发出信号的选项，说明为什么没有为展示出价
 *
 * Created by Zeki on 2022/12/24
 **/
@Getter
@AllArgsConstructor
public enum NoBidReasonCodeEnum {

    UNKNOWN_ERROR("0", "Unknown Error"),
    TECHNICAL_ERROR("1", "Technical Error"),
    INVALID_REQUEST("2", "Invalid Request"),
    KNOWN_WEB_SPIDER("3", "Known Web Spider"),
    SUSPECTED_NON_HUMAN_TRAFFIC("4", "Suspected Non-Human Traffic"),
    CLOUD_DATA_CENTER_PROXY_IP("5", "Cloud, Data center, or Proxy IP"),
    UNSUPPORTED_DEVICE("6", "Unsupported Device"),
    BLOCKED_PUBLISHER_OR_SITE("7", "Blocked Publisher or Site"),
    UNMATCHED_USER("8", "Unmatched User"),
    DAILY_READER_CAP_MET("9", "Daily Reader Cap Met"),
    DAILY_DOMAIN_CAP_MET("10", "Daily Domain Cap Met");

    private final String value;
    private final String desc;
}
