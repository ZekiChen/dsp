package com.tecdo.domain.biz.dto;

import com.tecdo.transform.ResponseTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class AdDTOWrapper {

  public AdDTOWrapper(String impId, String bidId, AdDTO adDTO) {
    this.impId = impId;
    this.bidId = bidId;
    this.adDTO = adDTO;
  }

  private AdDTO adDTO;

  // =====================以下是经过计算/逻辑判断后填充的属性==========================
  // 一个AdDTO会被多个task或者多次请求使用，除非每次获取时copy一份出来，否则不能修改，所以以下属性需要包装类来进行修改

  /**
   * 预估的ctr
   */
  private Double pCtr;

  /**
   * 英语预估ctr的模型版本
   */
  private String pCtrVersion;

  /**
   * 预估的cvr
   */
  private Double pCvr;

  /**
   * 英语预估cvr的模型版本
   */
  private String pCvrVersion;

  /**
   * 出价cpc
   */
  private BigDecimal bidPrice;

  /**
   * 广告主 lazada rta token
   */
  private String rtaToken;

  /**
   * imp的id
   */
  private String impId;

  /**
   * bidId,同时也是taskId
   */
  private String bidId;

  /**
   * 是否请求了 rta
   */
  private int rtaRequest;

  /**
   * 是否为 rta 匹配
   */
  private int rtaRequestTrue;

  /**
   * 落地页，目前AE RTA会用到
   */
  private String landingPage;
  private String deeplink;
  private boolean useDeeplink;

  private ResponseTypeEnum responseTypeEnum = ResponseTypeEnum.NORMAL;

}
