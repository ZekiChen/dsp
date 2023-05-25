package com.tecdo.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class CampaignRtaDTO implements Serializable {


  private String advCampaignId;
  private String channel;
  private String country;

}