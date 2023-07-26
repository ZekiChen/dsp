package com.tecdo.util;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class StringConfigUtil {

  private static Map<String, String> countryCodeMap;
  private static String bannerTemplate;
  private static String forceBannerTemplate;
  private static String videoVast4Template;

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

    try (InputStream is = StringConfigUtil.class.getResourceAsStream("/force-banner.html")) {
      byte[] bytes = ByteStreams.toByteArray(is);
      forceBannerTemplate = new String(bytes, StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
    }

    try (InputStream is = StringConfigUtil.class.getResourceAsStream("/video-vast4.xml")) {
      byte[] bytes = ByteStreams.toByteArray(is);
      videoVast4Template = new String(bytes, StandardCharsets.UTF_8);
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

  public static String getForceBannerTemplate() {
    return forceBannerTemplate;
  }

  public static String getVideoVast4Template() {
    return videoVast4Template;
  }

}
