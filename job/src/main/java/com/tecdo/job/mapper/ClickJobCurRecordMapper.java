package com.tecdo.job.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.job.domain.entity.ClickJobCurRecord;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@DS("mysql-ddj")
public interface ClickJobCurRecordMapper extends BaseMapper<ClickJobCurRecord> {

  @Select(
    "select cur from click_job_cur_record where country = #{country} and os = #{os} and package_name = #{packageName}" +
    "and recall_type = #{recallType}")
  Long getCur(String country, String os, String packageName, String recallType);

  @Insert("insert into click_job_cur_record(country,os,package_name,recall_type,cur) values " +
          "(#{country},#{os},#{packageName},#{recallType},#{offset})")
  void createCur(String country, String os, String packageName, String recallType, Long offset);

  @Update(
    "update click_job_cur_record set cur = #{offset} where country = #{country} and os = #{os} and package_name = #{packageName}" +
    "and recall_type = #{recallType}")
  void updateCur(String country, String os, String packageName, String recallType, Long offset);

}
