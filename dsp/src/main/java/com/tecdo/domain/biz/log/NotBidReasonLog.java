package com.tecdo.domain.biz.log;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotBidReasonLog implements Serializable {

  public NotBidReasonLog(String bidId, Integer adId, String reason) {
    this.bidId = bidId;
    this.adId = adId;
    this.reason = reason;
  }

  @JsonProperty("bid_id")
  private String bidId;

  @JsonProperty("ad_id")
  private Integer adId;

  @JsonProperty("reason")
  private String reason;

}
