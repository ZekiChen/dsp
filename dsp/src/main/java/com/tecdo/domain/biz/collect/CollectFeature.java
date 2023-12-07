package com.tecdo.domain.biz.collect;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CollectFeature {
  private String userAgent;
  private Boolean webdriver;
  private List<String> plugins;
  private List<String> mimeTypes;
  private String platform;
  private List<Boolean> phantomJSFeatures;
  private Boolean nightmareJS;
  private List<Boolean> seleniumFeatures;
  private Map<String, Object> webGL;
  private Map<String, Object> canvas;
  private Map<String, Object> webRTC;
  private Map<String, Object> fonts;
  private Map<String, Object> cdp;

  private Boolean selenium;
  private Boolean phantomJS;

  private String bidId;

  private Integer affiliateId;

  private Integer adGroupId;

  private String bundle;

  private String schain;

  private String deviceId;

  private String ip;

  private String ipFromImp;
}
