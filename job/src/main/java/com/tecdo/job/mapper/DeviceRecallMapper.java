package com.tecdo.job.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.job.domain.entity.DeviceRecall;

import org.apache.ibatis.annotations.Select;

import java.util.List;

@DS("doris-ads")
public interface DeviceRecallMapper extends BaseMapper<DeviceRecall> {

  @Select(
    "select time_millis,recall_tag,device_id,country,os,package_name,recall_type,device_make,device_model,osv,ip,ua,lang,etl_time,device_first_time,device_last_time from `recall_device` " +
    "WHERE country = #{country} and os = #{os} and package_name = #{packageName} and recall_type = #{recallType} " +
    "and recall_tag >= #{recallTag} and status = 1 and time_millis > #{offset} ORDER BY time_millis limit #{size}")
  List<DeviceRecall> query(String country,
                           String os,
                           String packageName,
                           Integer recallType,
                           Integer recallTag,
                           Long offset,
                           Integer size);

  @Select(
    "select time_millis,recall_tag,device_id,country,os,package_name,recall_type,status from `recall_device` " +
    "WHERE country = #{country} and os = #{os} and package_name = #{packageName} and recall_type = #{recallType} " +
    "and recall_tag >= #{recallTag} and time_millis > #{offset} ORDER BY time_millis limit #{size}")
  List<DeviceRecall> queryForUpdate(String country,
                                    String os,
                                    String packageName,
                                    Integer recallType,
                                    Integer recallTag,
                                    Long offset,
                                    Integer size);

  @Select(
    "select time_millis,recall_tag,device_id,country,os,package_name,recall_type,device_make,device_model,osv,ip,ua,lang,etl_time,device_first_time,device_last_time from `recall_device_by_model_train` " +
    "WHERE country = #{country} and os = #{os} and package_name = #{packageName} and recall_type = #{recallType} and version = #{version} " +
    "and recall_tag >= #{recallTag} and status = 1 and time_millis > #{offset} ORDER BY time_millis limit #{size}")
  List<DeviceRecall> queryFromModel(String country,
                                    String os,
                                    String packageName,
                                    Integer recallType,
                                    String version,
                                    Integer recallTag,
                                    Long offset,
                                    Integer size);

  @Select(
    "select time_millis,recall_tag,device_id,country,os,package_name,device_make,device_model,osv,ip,ua,lang from `recall_device_by_model_train` " +
    "WHERE country = #{country} and os = #{os} and package_name = #{packageName} and version = #{version} " +
    "and recall_tag >= #{recallTag} and status = 1 and time_millis > #{offset} ORDER BY time_millis limit #{size}")
  List<DeviceRecall> queryFromNewModel(String country,
                                    String os,
                                    String packageName,
                                    Integer recallType,
                                    String version,
                                    Integer recallTag,
                                    Long offset,
                                    Integer size);
}
