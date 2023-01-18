package com.tecdo.domain.biz.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AdDTOWrapper {

  private AdDTO adDTO;

  /**
   * 预估的ctr
   */
  private Double pCtr;

  /**
   * 出价cpc
   */
  private Double bidPrice;

  /**
   * 广告主 rta token
   */
  private String rtaToken;

  /**
   * imp的id
   */
  private String impId;

}
