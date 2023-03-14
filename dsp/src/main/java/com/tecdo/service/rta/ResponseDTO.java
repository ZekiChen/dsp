package com.tecdo.service.rta;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseDTO {

  private LazadaRtaData data;

  private String code;

  @JsonProperty("request_id")
  private String requestId;

  private String type;

  private String message;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public LazadaRtaData getData() {
    return data;
  }

  public void setData(LazadaRtaData data) {
    this.data = data;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
