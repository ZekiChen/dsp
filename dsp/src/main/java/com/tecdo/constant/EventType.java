package com.tecdo.constant;

import lombok.AllArgsConstructor;

/**
 * 事件类型
 * <p>
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

  BUDGETS_LOAD(1401, "budget list load"),
  BUDGETS_LOAD_RESPONSE(1402, "budget list load response"),
  BUDGETS_LOAD_ERROR(1403, "budget list load failure"),
  BUDGETS_LOAD_TIMEOUT(1404, "budget list load timeout"),

  AB_TEST_CONFIG_LOAD(1501, "ab test config load"),
  AB_TEST_CONFIG_LOAD_RESPONSE(1502, "ab test config load response"),
  AB_TEST_CONFIG_LOAD_ERROR(1503, "ab test config load failure"),
  AB_TEST_CONFIG_LOAD_TIMEOUT(1504, "ab test config load timeout"),

  GP_APP_LOAD(1601, "gp app list load"),
  GP_APP_LOAD_RESPONSE(1602, "gp app list load response"),
  GP_APP_LOAD_ERROR(1603, "ap app list load failure"),
  GP_APP_LOAD_TIMEOUT(1604, "gp app list load timeout"),

  IP_TABLE_LOAD(1701, "ip table load"),
  IP_TABLE_LOAD_RESPONSE(1702, "ip table load response"),
  IP_TABLE_LOAD_ERROR(1703, "ip table load failure"),
  IP_TABLE_LOAD_TIMEOUT(1704, "ip table load timeout"),

  AF_AUDIENCE_SYNC_TABLE_LOAD(1901, "af audience sync load"),
  AF_AUDIENCE_SYNC_LOAD_RESPONSE(1902, "af audience sync load response"),
  AF_AUDIENCE_SYNC_LOAD_ERROR(1903, "af audience sync load failure"),
  AF_AUDIENCE_SYNC_LOAD_TIMEOUT(1904, "af audience sync load timeout"),

  AFF_COUNTRY_BUNDLE_LIST_LOAD(2001, "aff country bundle list load"),
  AFF_COUNTRY_BUNDLE_LIST_LOAD_RESPONSE(2002, "aff country bundle list load response"),
  AFF_COUNTRY_BUNDLE_LIST_LOAD_ERROR(2003, "aff country bundle list load failure"),
  AFF_COUNTRY_BUNDLE_LIST_LOAD_TIMEOUT(2004, "aff country bundle list load timeout"),

  BUNDLE_DATA_LOAD(2101, "bundle data load"),
  BUNDLE_DATA_LOAD_RESPONSE(2102, "bundle data load response"),
  BUNDLE_DATA_LOAD_ERROR(2103, "bundle data load failure"),
  BUNDLE_DATA_LOAD_TIMEOUT(2104, "bundle data load timeout"),

  CHEATING_DATA_LOAD(2201, "cheating data load"),
  CHEATING_DATA_LOAD_RESPONSE(2202, "cheating data load response"),
  CHEATING_DATA_LOAD_ERROR(2203, "cheating data load failure"),
  CHEATING_DATA_LOAD_TIMEOUT(2204, "cheating data load timeout"),

  ONE_DATA_READY(1998, "a data finish init, enter ready, count increased"),
  NETTY_START(1999, "all data finish init, netty start"),

  RECEIVE_PING_REQUEST(3000, "receive ping request"),
  VALIDATE_BID_REQUEST(3001, "validate bid request"),
  RECEIVE_BID_REQUEST(3002, "receive bid request"),
  RECEIVE_WIN_NOTICE(3003, "receive win notice"),
  RECEIVE_IMP_NOTICE(3004, "receive imp notice"),
  RECEIVE_CLICK_NOTICE(3005, "receive click notice"),
  RECEIVE_PB_NOTICE(3006, "receive pb notice"),
  RECEIVE_IMP_INFO_NOTICE(3007, "receive imp info notice"),
  RECEIVE_LOSS_NOTICE(3008, "receive loss notice"),

  BID_TASK_FINISH(3101, "bid task finish"),
  BID_TASK_FAILED(3102, "bid task failed"),
  WAIT_TASK_RESPONSE_TIMEOUT(3103, "wait task response timeout"),

  WAIT_REQUEST_RTA_RESPONSE_TIMEOUT(3201, "wait request rta response timeout"),
  REQUEST_RTA_RESPONSE(3202, "request rta response"),
  WAIT_REQUEST_RTA_RESPONSE_ERROR(3203, "wait request rta response error"),

  WAIT_SORT_AD_TIMEOUT(3301, "wait sort ad timeout"),
  SORT_AD_RESPONSE(3302, "sort ad response"),

  BID_REQUEST_COMPLETE(3100, "bid request complete"),

  TASK_START(4001, "task start"),
  ADS_RECALL_FINISH(4002, "ads recall finish"),
  ADS_RECALL_ERROR(4003, "ads recall error"),
  ADS_RECALL_TIMEOUT(4004, "ads recall timeout"),

  PREDICT_FINISH(4101, "ctr predict finish"),
  PREDICT_ERROR(4102, "ctr predict error"),
  PREDICT_TIMEOUT(4103, "ctr predict timeout"),

  CALC_CPC_FINISH(4201, "calculate cpc finish"),
  CALC_CPC_ERROR(4202, "calculate cpc error"),
  CALC_CPC_TIMEOUT(4203, "calculate cpc timeout"),

  RESPONSE_RESULT(6001, "response result"),
  ;

  private final int code;
  private final String desc;

  @Override
  public String toString() {
    return code + " - " + desc;
  }
}
