package com.tecdo.adm.api.doris.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("cheating")
public class CheatingData {

  private Long hashCode;

  private String cheatKey;

  private String reason;


}
