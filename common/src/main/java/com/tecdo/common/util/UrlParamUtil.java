package com.tecdo.common.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

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
        Map<String, String> paramMap = GoogleURIParserAdapter.getInstance().parseURI(urlStr);
        return paramMap.get(key);
    }
}
