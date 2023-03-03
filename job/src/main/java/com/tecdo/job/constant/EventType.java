package com.tecdo.job.constant;

import lombok.AllArgsConstructor;

/**
 * 事件类型
 * <p>
 * Created by Zeki on 2022/12/27
 **/
@AllArgsConstructor
public enum EventType {

  SERVER_START(1001, "server start"),

  BUDGETS_LOAD(1101, "budget list load"),
  BUDGETS_LOAD_RESPONSE(1102, "budget list load response"),
  BUDGETS_LOAD_ERROR(1103, "budget list load failure"),
  BUDGETS_LOAD_TIMEOUT(1104, "budget list load timeout"),

  CAMPAIGNS_LOAD(1201, "campaign list load"),
  CAMPAIGNS_LOAD_RESPONSE(1202, "campaign list load response"),
  CAMPAIGNS_LOAD_ERROR(1203, "campaign list load failure"),
  CAMPAIGNS_LOAD_TIMEOUT(1204, "campaign list load timeout"),

  ONE_DATA_READY(1998, "a data finish init, enter ready, count increased"),
  NETTY_START(1999, "all data finish init, netty start"),

  RECEIVE_PING_REQUEST(3000, "receive ping request"),

  RESPONSE_RESULT(6001, "response result"),
  ;

  private final int code;
  private final String desc;

  @Override
  public String toString() {
    return code + " - " + desc;
  }
}
