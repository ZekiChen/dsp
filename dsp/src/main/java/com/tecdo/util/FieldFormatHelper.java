package com.tecdo.util;

import org.apache.commons.lang3.StringUtils;

public class FieldFormatHelper {

  public static String osFormat(String os) {
    if ("IOS".equalsIgnoreCase(os)) {
      return "IOS";
    }
    if ("Android".equalsIgnoreCase(os)) {
      return "Android";
    }
    return os;
  }

  public static String deviceMakeFormat(String deviceMake) {
    return StringUtils.toRootUpperCase(deviceMake);
  }

  public static String countryFormat(String country) {
    return StringUtils.toRootUpperCase(country);
  }

  public static String deviceModelFormat(String deviceModel) {
    return StringUtils.toRootUpperCase(deviceModel);
  }

  public static String languageFormat(String language) {
    return StringUtils.toRootUpperCase(language);
  }

}
