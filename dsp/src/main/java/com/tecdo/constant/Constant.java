package com.tecdo.constant;

public interface Constant {

    String QUESTION_MARK = "?";
    String EQUAL_MARK = "=";
    String AND_MARK = "&";
    String HEADER_IP_KEY = "X-Forwarded-For";
    String COMMA = ",";
    String ERROR_DEVICE_ID = "00000000-0000-0000-0000-000000000000";

    long TIMEOUT_WAIT_TASK_RESPONSE = 50;

    long TIMEOUT_WAIT_RTA_RESPONSE = 20;

    long TEN_MILLIS = 10;

    long TIMEOUT_LOAD_DB_CACHE_GENERAL = 5 * 1000L;
    long TIMEOUT_LOAD_DB_CACHE_AD_DTO = 10 * 1000L;
    long INTERVAL_RELOAD_DB_CACHE = 5 * 60 * 1000L;

    long TIMEOUT_ADS_RECALL = 50L;
    long TIMEOUT_PRE_DICT = 50L;
    long TIMEOUT_CALC_PRICE = 50L;
}
