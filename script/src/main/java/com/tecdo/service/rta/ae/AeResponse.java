package com.tecdo.service.rta.ae;

import com.tecdo.enums.AeCode;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by Zeki on 2023/4/3
 */
public class AeResponse<T> implements Serializable {

  private Integer code;
  private String message;
  private T data;

  public AeResponse() {
  }

  public AeResponse(Integer code, String message, T data) {
    this.code = code;
    this.message = message;
    this.data = data;
  }

  public AeResponse(AeCode aeCode) {
    this.code = aeCode.getCode();
    this.message = aeCode.getDesc();
  }

  public boolean succeed() {
    return Objects.equals(AeCode.SUCCESS.getCode(), this.code);
  }

  public static <T> AeResponse<T> data(T data) {
    return new AeResponse<>(AeCode.SUCCESS.getCode(), null, data);
  }

  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }
}
