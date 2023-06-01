package com.tecdo.job.util;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class StringConfigUtil {

    private static Map<String, String> countryCodeMap;

    static {
        try (InputStream is = StringConfigUtil.class.getResourceAsStream("/country-code.json")) {
            byte[] bytes = ByteStreams.toByteArray(Objects.requireNonNull(is));
            String str = new String(bytes, StandardCharsets.UTF_8);
            countryCodeMap = JsonHelper.parseMap(str, String.class, String.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getCountryCode3(String code2) {
        return countryCodeMap.get(code2);
    }

}
