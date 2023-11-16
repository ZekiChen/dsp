package com.tecdo.common.util;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Url请求参数解析
 *
 * Created by Zeki on 2023/11/16
 */
public class UrlParamUtil {

    public static String getValue(String urlStr, String key) {
        if (urlStr == null || "".equals(urlStr)) {
            return null;
        }
        try {
            URL url = new URL(urlStr);
            String query = url.getQuery();
            String[] params = query.split("&");
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair[0].equals(key)) {
                    return pair[1];
                }
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Your URL is not valid. Please check it and try again.");
        }
        return null;
    }
}
