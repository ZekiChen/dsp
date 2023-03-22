package com.tecdo.starter.mp.util;

import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Created by Zeki on 2023/3/22
 */
public class MpBigTool {

    public static boolean hasEmpty(Object... os) {
        for (Object o : os) {
            if (ObjectUtils.isEmpty(o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 字符串转 int，为空则返回默认值
     *
     * <pre>
     *   $.toInt(null, 1) = 1
     *   $.toInt("", 1)   = 1
     *   $.toInt("1", 0)  = 1
     * </pre>
     *
     * @param str          the string to convert, may be null
     * @param defaultValue the default value
     * @return the int represented by the string, or the default if conversion fails
     */
    public static int toInt(@Nullable final Object str, final int defaultValue) {
        return MpNumUtil.toInt(String.valueOf(str), defaultValue);
    }

    /**
     * 转换为String数组<br>
     *
     * @param str 被转换的值
     * @return 结果
     */
    public static String[] toStrArray(String str) {
        return toStrArray(",", str);
    }

    /**
     * 转换为String数组<br>
     *
     * @param split 分隔符
     * @param str   被转换的值
     * @return 结果
     */
    public static String[] toStrArray(String split, String str) {
        if (!StringUtils.hasText(str)) {
            return new String[]{};
        }
        return str.split(split);
    }
}
