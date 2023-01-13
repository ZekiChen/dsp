package com.tecdo.service.rta;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LazadaTarget {

  @JsonProperty("campaign_id")
  private String advCampaignId;

  private boolean target;


  public boolean isTarget() {
    return target;
  }

  public void setTarget(boolean target) {
    this.target = target;
  }

  public String getAdvCampaignId() {
    return advCampaignId;
  }

  public void setAdvCampaignId(String campaignId) {
    this.advCampaignId = campaignId;
  }
}















