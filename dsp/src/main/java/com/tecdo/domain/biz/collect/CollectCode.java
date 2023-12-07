package com.tecdo.domain.biz.collect;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CollectCode {
  private String code;

  private String bidId;

  private Integer affiliateId;

  private Integer adGroupId;

  private String bundle;

  private String schain;

  private String deviceId;

  private String ip;

  private String ipFromImp;
}
