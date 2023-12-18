package com.tecdo.domain.biz.collect;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CollectFeature {
  @JsonProperty("user_agent")
  private String userAgent;

  @JsonProperty("web_driver")
  private Boolean webdriver;

  private List<String> plugins;

  @JsonProperty("mime_types")
  private List<String> mimeTypes;

  private String platform;

  @JsonProperty("phantom_js_features")
  private List<Boolean> phantomJSFeatures;

  @JsonProperty("nightmare_js")
  private Boolean nightmareJS;

  @JsonProperty("selenium_features")
  private List<Boolean> seleniumFeatures;

  @JsonProperty("web_gl")
  private Map<String, Object> webGL;

  private Map<String, Object> canvas;

  @JsonProperty("web_rtc")
  private Map<String, Object> webRTC;

  private Map<String, Object> fonts;

  private Map<String, Object> cdp;

  private Boolean selenium;

  @JsonProperty("phantom_js")
  private Boolean phantomJS;

  @JsonProperty("bid_id")
  private String bidId;

  @JsonProperty("affiliate_id")
  private Integer affiliateId;

  @JsonProperty("ad_group_id")
  private Integer adGroupId;

  private String bundle;

  private String schain;

  @JsonProperty("device_id")
  private String deviceId;

  private String ip;

  @JsonProperty("ip_from_imp")
  private String ipFromImp;
}
