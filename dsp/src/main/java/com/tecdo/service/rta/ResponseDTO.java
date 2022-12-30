package com.tecdo.service.rta;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseDTO {

  private LazadaRtaData data;

  private String code;

  @JsonProperty("request_id")
  private String requestId;

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
}
