package com.tecdo.domain.biz.dto;

import com.tecdo.adm.api.delivery.entity.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * 将 ad-group-campaign 数据打平，平铺到 AdDTO 中
 * <p>
 * Created by Zeki on 2022/12/29
 **/
@Setter
@Getter
@Builder
public class AdDTO implements Serializable {

  /**
   * 广告信息
   */
  private Ad ad;

  /**
   * AD关联的物料集，比如native广告就需要 logo 和 image
   */
  private Map<Integer, Creative> creativeMap;

  /**
   * 广告所属的组信息
   */
  private AdGroup adGroup;

  /**
   * group关联的定向条件集  attribute - condition
   */
  private Map<String, TargetCondition> conditionMap;

  /**
   * group关联的双阶段出价信息  stage - multiBidStrategy
   */
  private Map<Integer, MultiBidStrategy> twoStageBidMap;

  /**
   * 广告所属的 campaign 信息
   */
  private Campaign campaign;

  /**
   * campaign 关联的广告主的 campaign
   */
  private CampaignRtaInfo campaignRtaInfo;

  /**
   * 广告主信息
   */
  private Adv adv;

}
