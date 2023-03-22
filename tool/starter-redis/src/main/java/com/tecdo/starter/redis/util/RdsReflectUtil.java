package com.tecdo.starter.redis.util;

import org.springframework.lang.Nullable;

import java.lang.reflect.Field;

/**
 * Created by Zeki on 2023/3/22
 */
public class RdsReflectUtil {

    /**
     * 获取 类属性
     * @param clazz 类信息
     * @param fieldName 属性名
     * @return Field
     */
    @Nullable
    public static Field getField(Class<?> clazz, String fieldName) {
        while (clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
