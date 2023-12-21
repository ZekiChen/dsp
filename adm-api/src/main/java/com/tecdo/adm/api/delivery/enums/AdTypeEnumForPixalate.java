package com.tecdo.adm.api.delivery.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 针对pixalate上报数据使用
 **/
@Getter
@AllArgsConstructor
public enum AdTypeEnumForPixalate {

  Desktop("Desktop"),
  Desktop_Video("Desktop_Video"),
  Desktop_Native("Desktop_Native"),
  Mobile_Web("Mobile_Web"),
  Mobile_Web_Video("Mobile_Web_Video"),
  Mobile_Web_Native("Mobile_Web_Native"),
  Mobile_InApp("Mobile_InApp"),
  Mobile_InApp_Video("Mobile_InApp_Video"),
  Mobile_InApp_Native("Mobile_InApp_Native"),
  Email_Display("Email_Display"),
  Email_Video("Email_Video"),
  CTV_InApp_Video("CTV_InApp_Video");

  private final String desc;


  public static AdTypeEnumForPixalate parse(Integer adType) {
    switch (adType) {
      case 1:
        return Mobile_InApp;
      case 2:
        return Mobile_InApp_Video;
      case 4:
        return Mobile_InApp_Native;
      default:
        return Mobile_InApp;
    }
  }

}
