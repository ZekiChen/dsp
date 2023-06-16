package com.tecdo.domain.biz.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class PredictRequest implements Serializable {

  /**
   * 用来区分参与预估的广告，不参与模型预估
   */
  private Integer adId;


  /**
   * adx的id
   */
  private Integer affiliateId;

  /**
   * 广告类型
   */
  private String adFormat;

  /**
   * 广告高度
   */
  private Integer adHeight;

  /**
   * 广告宽度
   */
  private Integer adWidth;

  /**
   * Android / IOS / 其他
   */
  private String os;

  /**
   * 系统版本
   */
  private String osv;

  /**
   * 设备制造商
   */
  private String deviceMake;

  /**
   * 流量所在的包名
   */
  private String bundleId;


  /**
   * 国家三字码
   */
  private String country;

  /**
   * 网络链接类型
   */
  private Integer connectionType;

  /**
   * 设备型号
   */
  private String deviceModel;

  /**
   * 运营商
   */
  private String carrier;
  /**
   * 素材id
   */
  private Integer creativeId;

  /**
   * 广告底价
   */
  private Double bidFloor;

  /**
   * RTA人群特征
   */
  private Integer feature1;


  /**
   * 投放的产品的包名
   */
  @JSONField(name = "package")
  @JsonProperty("package")
  private String packageName;

  /**
   * 投放的产品的category
   */
  private String category;

  private Integer pos;

  private String domain;

  private Integer instl;

  private List<String> cat;

  private String ip;

  private String ua;

  private String lang;

  private String deviceId;

  private List<String> bundleIdCategory;

  private List<String> bundleIdTag;

  private String bundleIdScore;

  private Long bundleIdDownload;

  private Long bundleIdReview;

  private String tagId;

}
