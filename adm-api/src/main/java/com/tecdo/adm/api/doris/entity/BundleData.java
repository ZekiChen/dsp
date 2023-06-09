package com.tecdo.adm.api.doris.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BundleData implements Serializable {

  private String country;

  private String bundle;

  private String adFormat;

  private String adWidth;

  private String adHeight;

  private Integer impCount;

  private Double winRate;

  private Double bidPrice;

  private Double bidFloor;

  private Double k;

  private Double oldK;


}
