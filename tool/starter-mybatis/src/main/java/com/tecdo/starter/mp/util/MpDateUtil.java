package com.tecdo.starter.mp.util;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by Zeki on 2023/3/22
 */
public class MpDateUtil {

    public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * 将字符串转换为时间
     *
     * @param dateStr 时间字符串
     * @param pattern 表达式
     * @return 时间
     */
    public static Date parse(String dateStr, String pattern) {
        MpConcurrentDateFormat format = MpConcurrentDateFormat.of(pattern);
        try {
            return format.parse(dateStr);
        } catch (ParseException e) {
            throw MpExceptions.unchecked(e);
        }
    }
}
