package com.tecdo.job.handler.sdk.lazada;

import com.google.common.base.MoreObjects;
import com.tecdo.job.domain.entity.DeviceRecall;
import com.tecdo.job.util.JsonHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import cn.hutool.core.date.DateUtil;

public class SdkLogger {

  private final static Logger sdkLogger = LoggerFactory.getLogger("sdk_log");

  public static void log(DeviceRecall deviceRecall, String clickId) {
    sdkLogger.info(JsonHelper.toJSONString(buildSdkLog(deviceRecall, clickId)));
  }

  private static SdkLog buildSdkLog(DeviceRecall deviceRecall, String clickId) {
    Long timeMillis = deviceRecall.getTimeMillis();
    Date etlTime = deviceRecall.getEtlTime();
    // 因为doris的etlTime是东八区的时间，程序是0时区，读取来后被当成是0时区，所以需要减去8小时
    long lastTime = etlTime.getTime() - 8 * 60 * 60 * 1000;
    String deviceFirstTime = DateUtil.format(new Date(timeMillis), "yyyy-MM-dd_HH");
    String deviceLastTime = DateUtil.format(new Date(lastTime), "yyyy-MM-dd_HH");
    return SdkLog.builder()
                 .clickId(clickId)
                 .deviceId(deviceRecall.getDeviceId())
                 .recallTag(deviceRecall.getRecallTag())
                 .recallType(deviceRecall.getRecallType())
                 .country(deviceRecall.getCountry())
                 .os(deviceRecall.getOs())
                 .packageName(deviceRecall.getPackageName())
                 .deviceMake(deviceRecall.getDeviceMake())
                 .deviceModel(deviceRecall.getDeviceModel())
                 .osv(deviceRecall.getOsv())
                 .ip(deviceRecall.getIp())
                 .ua(deviceRecall.getUa())
                 .lang(MoreObjects.firstNonNull(deviceRecall.getLang(), "en"))
                 .deviceFirstTime(deviceFirstTime)
                 .deviceLastTime(deviceLastTime)
                 .build();
  }
}
