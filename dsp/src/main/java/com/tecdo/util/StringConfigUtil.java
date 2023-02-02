package com.tecdo.util;

import com.google.common.io.ByteStreams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

public class StringConfigUtil {

  private static Map<String, String> countryCodeMap;
  private static String bannerTemplate;
  private static final Properties PROPERTIES;

  static {
    try (InputStream is = StringConfigUtil.class.getResourceAsStream("/country-code.json")) {
      byte[] bytes = ByteStreams.toByteArray(is);
      String str = new String(bytes, StandardCharsets.UTF_8);
      countryCodeMap = JsonHelper.parseMap(str, String.class, String.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    try (InputStream is = StringConfigUtil.class.getResourceAsStream("/banner.html")) {
      byte[] bytes = ByteStreams.toByteArray(is);
      bannerTemplate = new String(bytes, StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
    }
    PROPERTIES = new Properties();

    try (InputStream is = StringConfigUtil.class.getResourceAsStream("/string.properties");
         InputStreamReader isr = new InputStreamReader(is);
         BufferedReader br = new BufferedReader(isr)) {
      PROPERTIES.load(br);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String getCountryCode(String code3) {
    return countryCodeMap.get(code3);
  }

  public static String getBannerTemplate() {
    return bannerTemplate;
  }

  public static String get(String key, String defaultValue) {
    return PROPERTIES.getProperty(key, defaultValue);
  }

  public static String get(String key) {
    return PROPERTIES.getProperty(key);
  }

}
