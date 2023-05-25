package com.tecdo.mapper.doris;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Select;

import java.util.List;

@DS("doris-ods")
public interface RequestMapper extends BaseMapper<String> {

  @Select("select device_id from `pac_dsp_request_v2` WHERE report_hour >= #{start} and report_hour < #{end} and country = #{country}")
  List<String> listDeviceId(String country, String start, String end);

}
