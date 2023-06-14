package com.tecdo.domain.biz.response;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * 预估响应 顶层对象模型
 **/
@Setter
@Getter
public class PredictResponse implements Serializable {

  /**
   * 广告id，用于关联请求中的adId
   */
  private Integer adId;

  /**
   * 预估的ctr
   */
  private Double pCtr;

  /**
   * 预估的cvr
   */
  private Double pCvr;

  private Double pCtcvrEvent1;

}
