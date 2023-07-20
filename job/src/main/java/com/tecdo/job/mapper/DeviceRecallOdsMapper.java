package com.tecdo.job.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tecdo.job.domain.entity.DeviceRecall;

import org.apache.ibatis.annotations.Insert;

import java.util.List;

@DS("doris-ods")
public interface DeviceRecallOdsMapper extends BaseMapper<DeviceRecall> {

  @Insert({
    "<script>insert into `recall_device`(recall_tag,recall_type,country,os,package_name,device_id,time_millis,device_make,device_model,osv,ip,ua,lang,status) values ",
    "<foreach item='i' collection='list' open='' separator=',' close=''>",
    "(#{i.recallTag},#{i.recallType},#{i.country},#{i.os},#{i.packageName},#{i.deviceId},#{i.timeMillis},#{i.deviceMake},#{i.deviceModel},#{i.osv},#{i.ip},#{i.ua},#{i.lang},#{i.status})",
    "</foreach></script>"
  })
  void update(List<DeviceRecall> list);
}
