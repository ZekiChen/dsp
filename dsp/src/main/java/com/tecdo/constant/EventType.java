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

  ONE_DATA_READY(1998, "a data finish init, enter ready, count increased"),
  NETTY_START(1999, "all data finish init, netty start"),

  RECEIVE_PING(3000, "receive ping"),
  VALIDATE_BID_REQUEST(3001, "validate bid request"),
  RECEIVE_BID_REQUEST(3002, "receive bid request"),

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

  CTR_PREDICT_FINISH(4101, "ctr predict finish"),
  CTR_PREDICT_ERROR(4102, "ctr predict error"),
  CTR_PREDICT_TIMEOUT(4103, "ctr predict timeout"),

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
