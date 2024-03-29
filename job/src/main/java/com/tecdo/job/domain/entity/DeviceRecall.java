package com.tecdo.job.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import lombok.Data;

@Data
@TableName("device_recall")
public class DeviceRecall {

  private Long timeMillis;

  private Integer recallTag;

  private String deviceId;

  private Integer recallType;

  private String country;

  private String os;

  private String packageName;

  private String deviceMake;

  private String deviceModel;

  private String osv;

  private String ip;

  private String ua;

  private String lang;

  private int status;

  private String deviceFirstTime;

  private String deviceLastTime;

  private Date etlTime;

  private String version;

  private String dataSource;

}
