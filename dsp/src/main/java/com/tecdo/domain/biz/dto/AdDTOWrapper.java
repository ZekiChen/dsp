package com.tecdo.domain.biz.dto;

import com.tecdo.adm.api.delivery.entity.Creative;
import com.tecdo.adm.api.delivery.enums.BidAlgorithmEnum;
import com.tecdo.adm.api.delivery.enums.BidStrategyEnum;
import com.tecdo.adm.api.delivery.enums.MultiBidStageEnum;
import com.tecdo.transform.ResponseTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class AdDTOWrapper {

  public AdDTOWrapper(String impId, String bidId, AdDTO adDTO, Integer pos) {
    this.impId = impId;
    this.bidId = bidId;
    this.adDTO = adDTO;
    this.pos = pos;
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
   * imp/pmp的bidfloor
   */
  private Float bidfloor;

  /**
   * pmp出价下对应的dealid
   */
  private String dealid;

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

  private BidAlgorithmEnum bidAlgorithmEnum = BidAlgorithmEnum.NO;

  private double random = Math.random();

  private Integer impFrequency = 0;
  private Integer clickFrequency = 0;
  private Integer impFrequencyHour = 0;
  private Integer clickFrequencyHour = 0;

  private MultiBidStageEnum bidStageEnum;
  private BidStrategyEnum bidStrategyEnum;
  private Double optPrice;
  private Double bidMultiplier;
  private Double bidProbability;
  private Boolean bundleTestEnable;

  private Creative image;
  private Integer pos;

}
