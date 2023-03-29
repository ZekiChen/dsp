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

  public static String cityFormat(String city) {
    return StringUtils.toRootUpperCase(city);
  }

  public static String regionFormat(String region) {
    return StringUtils.toRootUpperCase(region);
  }

  public static String deviceModelFormat(String deviceModel) {
    return StringUtils.toRootUpperCase(deviceModel);
  }

  public static String languageFormat(String language) {
    return StringUtils.toRootUpperCase(language);
  }

  public static String bundleIdFormat(String bundleId) {
    if (bundleId.contains("&")) {
      return bundleId.split("&")[0];
    }
    return bundleId;
  }

}
