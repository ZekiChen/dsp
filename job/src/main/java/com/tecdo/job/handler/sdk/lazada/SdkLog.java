package com.tecdo.job.handler.sdk.lazada;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class SdkLog implements Serializable {

  @JsonProperty("click_id")
  private String clickId;

  @JsonProperty("device_id")
  private String deviceId;

  @JsonProperty("recall_tag")
  private Integer recallTag;

  @JsonProperty("recall_type")
  private Integer recallType;

  private String country;

  private String os;

  @JsonProperty("package_name")
  private String packageName;

  @JsonProperty("device_make")
  private String deviceMake;

  @JsonProperty("device_model")
  private String deviceModel;

  private String osv;

  private String ip;

  private String ua;

  private String lang;

  @JsonProperty("device_first_time")
  private String deviceFirstTime;

  @JsonProperty("device_last_time")
  private String deviceLastTime;

  @JsonProperty("version")
  private String version;

  @JsonProperty("data_source")
  private String dataSource;


}
