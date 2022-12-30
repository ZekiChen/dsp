package com.tecdo.constant;

public interface Constant {

    String QUESTION_MARK = "?";
    String EQUAL_MARK = "=";
    String AND_MARK = "&";

    long TIMEOUT_WAIT_TASK_RESPONSE = 50;

    long TIMEOUT_LOAD_DB_CACHE_GENERAL = 5 * 1000L;
    long TIMEOUT_LOAD_DB_CACHE_AD_DTO = 10 * 1000L;
    long INTERVAL_RELOAD_DB_CACHE = 5 * 60 * 1000L;
}
