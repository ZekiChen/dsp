package com.tecdo.util;

import com.tecdo.enums.biz.PlacementTypeEnum;

import java.util.LinkedHashMap;

/**
 * 扩展位解析工具
 *
 * Created by Zeki on 2023/9/12
 */
public class ExtHelper {

    private final static String LOOKME_PLACEMENT_TYPE = "placementType";

    public static boolean isRewarded(Object ext) {
        try {
            LinkedHashMap<String, Object> extMap = (LinkedHashMap) ext;
            if (extMap != null && extMap.containsKey(LOOKME_PLACEMENT_TYPE)) {
                return PlacementTypeEnum.REWARDED.getDesc().equals(extMap.get(LOOKME_PLACEMENT_TYPE).toString());
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
