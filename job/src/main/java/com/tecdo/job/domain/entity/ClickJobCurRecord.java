package com.tecdo.job.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("click_job_cur_record")
public class ClickJobCurRecord {

  private Integer id;

  private String country;

  private String os;

  private String packageName;

  private String recallType;

  private Long cur;


}
