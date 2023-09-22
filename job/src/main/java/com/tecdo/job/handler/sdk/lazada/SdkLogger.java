package com.tecdo.job.handler.sdk.lazada;

import com.google.common.base.MoreObjects;
import com.tecdo.job.domain.entity.DeviceRecall;
import com.tecdo.job.util.JsonHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdkLogger {

  private final static Logger sdkLogger = LoggerFactory.getLogger("sdk_log");

  public static void log(DeviceRecall deviceRecall, String clickId) {
    sdkLogger.info(JsonHelper.toJSONString(buildSdkLog(deviceRecall, clickId)));
  }

  private static SdkLog buildSdkLog(DeviceRecall deviceRecall, String clickId) {
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
                 .version(deviceRecall.getVersion())
                 .dataSource(deviceRecall.getDataSource())
                 .build();
  }
}
