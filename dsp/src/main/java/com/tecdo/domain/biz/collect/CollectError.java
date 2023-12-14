package com.tecdo.domain.biz.collect;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CollectError {
  private String msg;

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
