package com.tecdo.starter.redis.util;

import org.springframework.util.ObjectUtils;

/**
 * Created by Zeki on 2023/3/22
 */
public class RdsBigTool {

    public static boolean hasEmpty(Object... os) {
        for (Object o : os) {
            if (ObjectUtils.isEmpty(o)) {
                return true;
            }
        }
        return false;
    }
}
