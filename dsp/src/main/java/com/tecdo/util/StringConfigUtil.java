package com.tecdo.util;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class StringConfigUtil {

    private static Map<String, String> countryCode3To2Map;
    private static Map<String, String> countryCode2To3Map;
    private static String bannerTemplate;
    private static String forceBannerTemplate;
    private static String forceBannerDebugTemplate;
    private static String videoVast4Template;

    static {
        try (InputStream is = StringConfigUtil.class.getResourceAsStream("/country/country-code.json")) {
            byte[] bytes = ByteStreams.toByteArray(is);
            String str = new String(bytes, StandardCharsets.UTF_8);
            countryCode3To2Map = JsonHelper.parseMap(str, String.class, String.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream is = StringConfigUtil.class.getResourceAsStream("/country/country-code2.json")) {
            byte[] bytes = ByteStreams.toByteArray(is);
            String str = new String(bytes, StandardCharsets.UTF_8);
            countryCode2To3Map = JsonHelper.parseMap(str, String.class, String.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream is = StringConfigUtil.class.getResourceAsStream("/banner/banner-encrypt.html")) {
            byte[] bytes = ByteStreams.toByteArray(is);
            bannerTemplate = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream is = StringConfigUtil.class.getResourceAsStream("/banner/force-banner-encrypt.html")) {
            byte[] bytes = ByteStreams.toByteArray(is);
            forceBannerTemplate = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream is = StringConfigUtil.class.getResourceAsStream("/banner/force-banner-debug-encrypt.html")) {
            byte[] bytes = ByteStreams.toByteArray(is);
            forceBannerDebugTemplate = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (InputStream is = StringConfigUtil.class.getResourceAsStream("/video/video-vast4.xml")) {
            byte[] bytes = ByteStreams.toByteArray(is);
            videoVast4Template = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getCountryCode2(String code3) {
        return countryCode3To2Map.get(code3);
    }

    public static String getCountryCode3(String code2) {
        return countryCode2To3Map.get(code2);
    }

    public static String getForceBannerTemplate() {
        return forceBannerTemplate;
    }

    public static String getForceBannerDebugTemplate() {
        return forceBannerDebugTemplate;
    }

    public static String getVideoVast4Template() {
        return videoVast4Template;
    }

    public static String getBannerTemplate() {
        return bannerTemplate;
    }

}
