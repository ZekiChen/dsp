package com.tecdo.job.handler.ddj.lazada;

import com.tecdo.job.domain.entity.DeviceRecall;
import com.tecdo.job.util.JsonHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DDJLogger {

  private final static Logger ddjLogger = LoggerFactory.getLogger("ddj_log");

  public static void log(DeviceRecall deviceRecall, String clickId) {
    ddjLogger.info(JsonHelper.toJSONString(buildDDJLog(deviceRecall, clickId)));
  }

  private static DDJLog buildDDJLog(DeviceRecall deviceRecall, String clickId) {
    return DDJLog.builder()
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
                 .lang(deviceRecall.getLang())
                 .build();
  }
}
