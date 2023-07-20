package com.tecdo.job.handler.sdk.lazada.match;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class LazadaRtaData {
  private String token;

  @JsonProperty("target_list")
  private List<LazadaTarget> targetList;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public List<LazadaTarget> getTargetList() {
    return targetList;
  }

  public void setTargetList(List<LazadaTarget> targetList) {
    this.targetList = targetList;
  }
}
