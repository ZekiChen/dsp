package com.tecdo.adm.api.foreign.ae.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Zeki on 2023/4/3
 */
@Getter
@AllArgsConstructor
public enum AeCode {

  SUCCESS(0, "成功"),
  PARAM_ERROR(1001, "请求参数错误"),
  SIGN_ERROR(1002, "签名参数校验错误"),
  QPS_LIMIT(2001, "QPS超限"),
  DAILY_LIMIT(2002, "每日超限"),
  INTERNAL_ERROR(3001, "内部处理异常"),
  SYSTEM_CLOSE(4001, "系统暂时关闭");

  private final Integer code;
  private final String desc;

}
